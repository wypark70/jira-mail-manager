<script lang="ts">
    import {
        Button,
        Datepicker,
        Input,
        Modal,
        Select,
        Table,
        TableBody,
        TableBodyCell,
        TableBodyRow,
        TableHead,
        TableHeadCell
    } from "flowbite-svelte";
    import JsonViewer from "$lib/components/JsonViewer.svelte";

    const springApiBaseUrl = import.meta.env.VITE_SPRING_API_BASE_URL;

    interface PageResponse<T> {
        content: T[];
        totalPages: number;
        totalElements: number;
        currentPage: number;
    }

    interface EmailHistory {
        id: number;
        originalEmailId: number;
        sender: string;
        subject: string;
        content: {
            body: string;
        };
        status: string;
        createdAt: string;
        sentAt: string;
        processorId: string;
        errorMessage: string | null;
        retryCount: number;
    }

    interface SearchFilters {
        subject: string;
        startDate: Date | undefined;
        endDate: Date | undefined;
    }

    interface PaginationState {
        currentPage: number;
        pageSize: number;
        sortBy: string;
        sortDirection: string;
    }

    let searchFilters: SearchFilters = {
        subject: '',
        startDate: undefined,
        endDate: undefined
    };

    let pagination: PaginationState = {
        currentPage: 0,
        pageSize: 10,
        sortBy: 'sentAt',
        sortDirection: 'desc'
    };

    const pageSizeOptions = [
        {value: 5, label: '5개씩 보기'},
        {value: 10, label: '10개씩 보기'},
        {value: 20, label: '20개씩 보기'},
        {value: 50, label: '50개씩 보기'}
    ];

    let mailHistoryPage: PageResponse<EmailHistory> = {
        content: [],
        totalPages: 0,
        totalElements: 0,
        currentPage: 0
    };

    let comumns = [
        { name: 'id', koName: 'ID' },
        { name: 'subject', koName: '제목' },
        { name: 'sender', koName: '발신자' },
        { name: 'sentAt', koName: '발신일' }
    ];

    // 모달 상태 관리
    let showModal = false;
    let selectedMail: EmailHistory | null = null;

    async function loadMailHistory(): Promise<void> {
        const {currentPage, pageSize, sortBy, sortDirection} = pagination;
        const {subject, startDate, endDate} = searchFilters;

        const params = new URLSearchParams({
            page: currentPage.toString(),
            size: pageSize.toString(),
            sort: sortBy,
            direction: sortDirection
        });

        if (subject) {
            params.append('subject', subject);
        }
        if (startDate) {
            params.append('startDate', formatDate(startDate));
        }
        if (endDate) {
            params.append('endDate', formatDate(endDate));
        }

        const response = await fetch(`${springApiBaseUrl}/email-history/search?${params}`);
        if (response.ok) {
            mailHistoryPage = await response.json();
        } else {
            console.error('메일 히스토리 로딩 실패:', await response.json());
        }
    }

    function formatDate(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    async function resetFilters(): Promise<void> {
        searchFilters.subject = '';
        searchFilters.startDate = undefined;
        searchFilters.endDate = undefined;
        await applyFilters();
    }

    async function changePage(newPage: number): Promise<void> {
        pagination.currentPage = newPage;
        await loadMailHistory();
    }

    async function changePageSize(event: Event): Promise<void> {
        const select = event.target as HTMLSelectElement;
        pagination.pageSize = parseInt(select.value);
        pagination.currentPage = 0;
        await loadMailHistory();
    }

    async function changeSort(column: string): Promise<void> {
        if (pagination.sortBy === column) {
            pagination.sortDirection = pagination.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            pagination.sortBy = column;
            pagination.sortDirection = 'asc';
        }
        await loadMailHistory();
    }

    async function fetchMailDetail(id: number) {
        const response = await fetch(`${springApiBaseUrl}/email-history/${id}`);
        if (response.ok) {
            selectedMail = await response.json();
            if (selectedMail) selectedMail.content.body = escapeHTML(selectedMail.content.body);
            showModal = true;
        } else {
            console.error('메일 상세 정보 로딩 실패:', await response.json());
        }
    }

    async function applyFilters(): Promise<void> {
        pagination.currentPage = 0;
        await loadMailHistory();
    }

    function escapeHTML(str: string): string {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return str.replace(/[&<>"']/g, (char) => map[char]);
    }

    // 초기 데이터 로드
    loadMailHistory();
</script>

<svelte:head>
    <title>Email Manager - Email History</title>
    <meta content="Email Manager - Email History" name="description"/>
</svelte:head>

<div class="container mx-auto px-4 py-8">
    <div class="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 class="text-3xl font-bold dark:text-white">Email History</h1>
    </div>

    <div class="mb-6 flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-700 p-4 shadow-sm dark:text-white">
        <div class="flex flex-wrap items-end gap-4">
            <!-- 제목 검색 -->
            <div class="flex-1 min-w-[200px]">
                <label class="mb-2 block text-sm font-medium" for="subject">제목</label>
                <Input
                        bind:value={searchFilters.subject}
                        class="w-full"
                        id="subject"
                        onchange={applyFilters}
                        placeholder="제목으로 검색..."
                        type="text"
                />
            </div>

            <!-- 발송일 시작 -->
            <div class="flex-1 min-w-[200px]">
                <label class="mb-2 block text-sm font-medium" for="startDate">발송일 시작</label>
                <Datepicker
                        bind:value={searchFilters.startDate}
                        class="w-full"
                        id="startDate"
                        onselect={applyFilters}
                        placeholder="발송일 시작 선택"
                />
            </div>

            <!-- 발송일 종료 -->
            <div class="flex-1 min-w-[200px]">
                <label class="mb-2 block text-sm font-medium" for="endDate">발송일 종료</label>
                <Datepicker
                        bind:value={searchFilters.endDate}
                        class="w-full"
                        id="endDate"
                        onselect={applyFilters}
                        placeholder="발송일 종료 선택"
                />
            </div>

            <!-- 버튼 그룹 -->
            <div class="flex gap-2">
                <Button color="primary" onclick={applyFilters}>
                    <svg class="h-4 w-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke-linecap="round"
                              stroke-linejoin="round" stroke-width="2"/>
                    </svg>
                    검색
                </Button>
                <Button color="alternative" onclick={resetFilters}>
                    초기화
                </Button>
            </div>
        </div>
    </div>

    {#if mailHistoryPage.content.length > 0}
        <Table shadow hoverable={true} class="rounded-lg overflow-hidden">
            <TableHead class="dark:text-white rounded-xl border-b border-black/20">
                {#each comumns as column}
                    <TableHeadCell onclick={() => changeSort(column.name)}>
                        <div class="flex cursor-pointer items-center gap-2 hover:text-blue-600">
                            {column.koName}
                            {#if pagination.sortBy === column.name}
                                <svg class="h-4 w-4 transition-transform {pagination.sortDirection === 'desc' ? 'rotate-180' : ''}"
                                     fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                          d="M5 15l7-7 7 7"/>
                                </svg>
                            {/if}
                        </div>
                    </TableHeadCell>
                {/each}
            </TableHead>
            <TableBody class="dark:text-white">
                {#each mailHistoryPage.content as mail}
                    <TableBodyRow class="border-black/20">
                        <TableBodyCell>{mail.id}</TableBodyCell>
                        <TableBodyCell>
                            <button
                                    class="text-left hover:text-blue-600 dark:hover:text-blue-400"
                                    on:click={() => fetchMailDetail(mail.id)}
                            >
                                {mail.subject}
                            </button>
                        </TableBodyCell>
                        <TableBodyCell>{mail.sender}</TableBodyCell>
                        <TableBodyCell>{new Date(mail.sentAt).toLocaleString()}</TableBodyCell>
                    </TableBodyRow>
                {/each}
            </TableBody>
        </Table>
        <!-- 페이지네이션 섹션 -->
        <div class="mt-6 flex flex-col items-center justify-between gap-4 sm:flex-row dark:text-white">
            <p class="text-sm">
                <span class="font-medium">
                    {pagination.currentPage * pagination.pageSize + 1}
                </span>
                -
                <span class="font-medium">
                    {Math.min((pagination.currentPage + 1) * pagination.pageSize, mailHistoryPage.totalElements)}
                </span>
                <span class="mx-1">of</span>
                <span class="font-medium">{mailHistoryPage.totalElements}</span>
                개 항목
            </p>

            <nav class="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                <button
                        class="relative inline-flex items-center rounded-l-md px-2 py-2  ring-1 ring-inset focus:z-20 focus:outline-offset-0 disabled:text-gray-500/50"
                        disabled={pagination.currentPage === 0}
                        on:click={() => changePage(0)}
                >
                    <span class="sr-only">처음으로</span>
                    <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd"
                              d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                              clip-rule="evenodd"/>
                    </svg>
                </button>

                <button
                        class="relative inline-flex items-center px-3 py-2 text-sm font-medium  ring-1 ring-inset focus:z-20 focus:outline-offset-0 disabled:text-gray-500/50"
                        disabled={pagination.currentPage === 0}
                        on:click={() => changePage(pagination.currentPage - 1)}
                >
                    이전
                </button>

                {#each Array(mailHistoryPage.totalPages) as _, i}
                    {#if i === pagination.currentPage ||
                    i === 0 ||
                    i === mailHistoryPage.totalPages - 1 ||
                    (i >= pagination.currentPage - 1 && i <= pagination.currentPage + 1)}
                        <button
                                class="relative inline-flex items-center px-4 py-2 text-sm font-semibold ring-1 ring-inset {
                                pagination.currentPage === i
                                    ? 'z-10 bg-gray-300 dark:bg-gray-700'
                                    : 'focus:z-20 focus:outline-offset-0'
                            }"
                                on:click={() => changePage(i)}
                                aria-current={pagination.currentPage === i ? 'page' : undefined}
                        >
                            {i + 1}
                        </button>
                    {:else if (i === pagination.currentPage - 2 || i === pagination.currentPage + 2)}
                        <span class="relative inline-flex items-center px-4 py-2 text-sm font-semibold ring-1 ring-inset focus:outline-offset-0">
                            ...
                        </span>
                    {/if}
                {/each}

                <button
                        class="relative inline-flex items-center px-3 py-2 text-sm font-medium ring-1 ring-inset focus:z-20 focus:outline-offset-0 disabled:text-gray-500/50"
                        disabled={pagination.currentPage === mailHistoryPage.totalPages - 1}
                        on:click={() => changePage(pagination.currentPage + 1)}
                >
                    다음
                </button>

                <button
                        class="relative inline-flex items-center rounded-r-md px-2 py-2 ring-1 ring-inset focus:z-20 focus:outline-offset-0 disabled:text-gray-500/50"
                        disabled={pagination.currentPage === mailHistoryPage.totalPages - 1}
                        on:click={() => changePage(mailHistoryPage.totalPages - 1)}
                >
                    <span class="sr-only">마지막으로</span>
                    <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd"
                              d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                              clip-rule="evenodd"/>
                    </svg>
                </button>
                <Select
                        bind:value={pagination.pageSize}
                        onchange={changePageSize}
                        class="ml-4"
                >
                    {#each pageSizeOptions as option}
                        <option value={option.value}>{option.label}</option>
                    {/each}
                </Select>
            </nav>
        </div>
    {:else}
        <div class="rounded-lg border p-12 text-center shadow-sm text-gray-200 dark:text-gray-700">
            <svg
                    class="mx-auto h-16 w-16 text-black dark:text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
            >
                <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="1.5"
                        d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
                />
            </svg>
            <h3 class="mt-4 text-lg font-medium text-black dark:text-white">조건에 맞는 메일 히스토리가 없습니다.</h3>
        </div>
    {/if}
</div>

<Modal
        autoclose
        bind:open={showModal}
        class="w-full"
        size="xl"
>
    {#if selectedMail}
        <div class="p-4">
            <h2 class="mb-4 text-2xl font-bold">메일 히스토리 상세 정보</h2>

            <div class="grid gap-4">
                <div class="grid grid-cols-3 gap-4 border-b pb-2">
                    <div class="font-semibold">ID</div>
                    <div class="col-span-2">{selectedMail.id}</div>
                </div>

                <div class="grid grid-cols-3 gap-4 border-b pb-2">
                    <div class="font-semibold">제목</div>
                    <div class="col-span-2">{selectedMail.subject}</div>
                </div>

                <div class="grid grid-cols-3 gap-4 border-b pb-2">
                    <div class="font-semibold">보낸 사람</div>
                    <div class="col-span-2">{selectedMail.sender}</div>
                </div>

                <div class="grid grid-cols-3 gap-4 border-b pb-2">
                    <div class="font-semibold">처리자 ID</div>
                    <div class="col-span-2">{selectedMail.processorId}</div>
                </div>

                <div class="grid grid-cols-3 gap-4 border-b pb-2">
                    <div class="font-semibold">재시도 횟수</div>
                    <div class="col-span-2">{selectedMail.retryCount}</div>
                </div>

                <div class="grid grid-cols-3 gap-4 border-b pb-2">
                    <div class="font-semibold">발송 시간</div>
                    <div class="col-span-2">{new Date(selectedMail.sentAt).toLocaleString()}</div>
                </div>

                {#if selectedMail.errorMessage}
                    <div class="grid grid-cols-3 gap-4 border-b pb-2">
                        <div class="font-semibold">오류 메시지</div>
                        <div class="col-span-2 text-red-600">{selectedMail.errorMessage}</div>
                    </div>
                {/if}
            </div>

            <JsonViewer json={selectedMail}/>

            <div class="mt-4 flex justify-end gap-2">
                <Button color="alternative" onclick={() => showModal = false}>
                    닫기
                </Button>
            </div>
        </div>
    {/if}
</Modal>