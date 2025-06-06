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
    import {page} from '$app/state';
    import JsonViewer from "$lib/components/JsonViewer.svelte";
	import type { E } from "vitest/dist/chunks/environment.d.Dmw5ulng.js";

    const springApiBaseUrl = import.meta.env.VITE_SPRING_API_BASE_URL;

    interface PageResponse<T> {
        content: T[];
        totalPages: number;
        totalElements: number;
        currentPage: number;
    }

    interface EmailQueue {
        id: number;
        recipient: string;
        sender: string;
        status: string;
        subject: string;
        content: {
            body: string;
        };
        createdAt: string;
        sentAt: string | null;
    }

    interface SearchFilters {
        status: string;
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

    // 검색 필터에 날짜 범위 추가
    let searchFilters: SearchFilters = {
        status: page.url.searchParams.get('status') || '',
        subject: '',
        startDate: undefined,  // 시작일
        endDate: undefined     // 종료일
    };

    // 상태 옵션 정의
    const statusOptions = [
        {value: 'QUEUED', label: 'QUEUED'},
        {value: 'RETRY', label: 'RETRY'},
        {value: 'SENT', label: 'SENT'},
        {value: 'FAILED', label: 'FAILED'}
    ];

    let pagination: PaginationState = {
        currentPage: 0,
        pageSize: 10,
        sortBy: 'createdAt',
        sortDirection: 'desc'
    };

    const pageSizeOptions = [
        {value: 5, label: '5개씩 보기'},
        {value: 10, label: '10개씩 보기'},
        {value: 20, label: '20개씩 보기'},
        {value: 50, label: '50개씩 보기'}
    ];

    let mailQueuePage: PageResponse<EmailQueue> = {
        content: [],
        totalPages: 0,
        totalElements: 0,
        currentPage: 0
    };

    let columns = [
        {name: 'ID', koName: 'ID'},
        {name: 'subject', koName: '제목'},
        {name: 'sender', koName: '발신자'},
        {name: 'status', koName: '상태'},
        {name: 'createdAt', koName: '생성일'}
    ];

    // 모달 상태 관리
    let showModal = false;
    let selectedMail: EmailQueue | null = null;

    // loadMailQueue 함수 수정
    async function loadMailQueue(): Promise<void> {
        const {currentPage, pageSize, sortBy, sortDirection} = pagination;
        const {status, subject, startDate, endDate} = searchFilters;

        // 검색 파라미터 구성
        const params = new URLSearchParams({
            page: currentPage.toString(),
            size: pageSize.toString(),
            sort: sortBy,
            direction: sortDirection
        });

        if (status) {
            params.append('status', status);
        }
        if (subject) {
            params.append('subject', subject);
        }
        if (startDate) {
            params.append('startDate', formatDate(startDate));
        }
        if (endDate) {
            params.append('endDate', formatDate(endDate));
        }

        const response = await fetch(`${springApiBaseUrl}/email-queue/search?${params}`);
        if (response.ok) {
            mailQueuePage = await response.json();
        } else {
            console.error('메일 큐 로딩 실패:', await response.json());
        }
    }

    function formatDate(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }


    // 필터 초기화 함수 수정
    async function resetFilters(): Promise<void> {
        searchFilters.status = '';
        searchFilters.subject = '';
        searchFilters.startDate = undefined
        searchFilters.endDate = undefined;
        await applyFilters();
    }

    async function changePage(newPage: number): Promise<void> {
        pagination.currentPage = newPage;
        await loadMailQueue();
    }

    async function changePageSize(event: Event): Promise<void> {
        const select = event.target as HTMLSelectElement;
        pagination.pageSize = parseInt(select.value);
        pagination.currentPage = 0; // 페이지 크기 변경시 첫 페이지로 이동
        await loadMailQueue();
    }

    async function changeSort(column: string): Promise<void> {
        if (pagination.sortBy === column) {
            pagination.sortDirection = pagination.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            pagination.sortBy = column;
            pagination.sortDirection = 'asc';
        }
        await loadMailQueue();
    }

    // 메일 상세 정보를 가져오는 함수
    async function fetchMailDetail(id: number) {
        const response = await fetch(`${springApiBaseUrl}/email-queue/${id}`);
        if (response.ok) {
            selectedMail = await response.json();
            showModal = true;
        } else {
            console.error('메일 상세 정보 로딩 실패:', await response.json());
        }
    }

    // 필터 적용 함수 수정
    async function applyFilters(): Promise<void> {
        pagination.currentPage = 0;
        await loadMailQueue();
    }

    function escapeHTML(str: string): string {
        const tempElement = document.createElement('div');
        tempElement.textContent = str;
        return tempElement.innerHTML;
    }

    function escapeContent(emailQue:EmailQueue): EmailQueue {
        return {
            ...emailQue,
            content: {
                ...emailQue.content,
                body: escapeHTML(emailQue.content.body)
            }
        };
    }

    // 초기 데이터 로드
    loadMailQueue();
</script>

<svelte:head>
    <title>Email Manager - Email Queue</title>
    <meta content="Email Manager - Email Queue" name="description"/>
</svelte:head>

<div class="container mx-auto px-4 py-8">
    <!-- 헤더 섹션 -->
    <div class="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 class="text-3xl font-bold dark:text-white">Email Queue</h1>
    </div>

    <div class="mb-6 flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-700 p-4 shadow-sm dark:text-white">
        <div class="flex flex-wrap items-end gap-4">
            <!-- 상태 필터 -->
            <div class="flex-1 min-w-[200px]">
                <label class="mb-2 block text-sm font-medium" for="status">상태</label>
                <Select
                        bind:value={searchFilters.status}
                        class="w-full"
                        id="status"
                        onchange={applyFilters}
                >
                    {#each statusOptions as option}
                        <option value={option.value}>{option.label}</option>
                    {/each}
                </Select>
            </div>

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

            <!-- 시작일 -->
            <div class="flex-1 min-w-[200px]">
                <label class="mb-2 block text-sm font-medium" for="startDate">생성일 시작</label>
                <Datepicker
                        bind:value={searchFilters.startDate}
                        class="w-full"
                        id="startDate"
                        onselect={applyFilters}
                        placeholder="생성일 시작 선택"
                />
            </div>

            <!-- 종료일 -->
            <div class="flex-1 min-w-[200px]">
                <label class="mb-2 block text-sm font-medium" for="endDate">생성일 종료</label>
                <Datepicker
                        bind:value={searchFilters.endDate}
                        class="w-full"
                        id="endDate"
                        onselect={applyFilters}
                        placeholder="생성일 종료 선택"
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

    {#if mailQueuePage.content.length > 0}
        <!-- 테이블 섹션 -->
        <Table shadow hoverable={true} class="rounded-lg overflow-hidden">
            <TableHead class="dark:text-white border-b border-black/20">
                {#each columns as column}
                    <TableHeadCell onclick={() => changeSort(column.name)}>
                        <div class="flex cursor-pointer items-center gap-2 hover:text-blue-600">
                            {column.koName}
                            {#if pagination.sortBy === column.name}
                                <svg class="h-4 w-4 transition-transform {pagination.sortDirection === 'desc' ? 'rotate-180' : ''}"
                                     fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                          d="M5 15l7-7 7 7"/>
                                </svg>
                            {:else}
                                <svg class="h-4 w-4 opacity-0 transition-opacity group-hover:opacity-100"
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
                {#each mailQueuePage.content as mail}
                    <!-- 테이블 내용 부분 수정 -->
                    <TableBodyRow class="border-black/20">
                        <TableBodyCell>{mail.id}</TableBodyCell>
                        <TableBodyCell>
                            <!-- 제목을 클릭 가능한 버튼으로 변경 -->
                            <button
                                    class="text-left hover:text-blue-600 dark:hover:text-blue-400"
                                    on:click={() => fetchMailDetail(mail.id)}
                            >
                                {mail.subject}
                            </button>
                        </TableBodyCell>
                        <TableBodyCell>{mail.sender}</TableBodyCell>
                        <TableBodyCell>{mail.status}</TableBodyCell>
                        <TableBodyCell>{new Date(mail.createdAt).toLocaleString()}</TableBodyCell>
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
                    {Math.min((pagination.currentPage + 1) * pagination.pageSize, mailQueuePage.totalElements)}
                </span>
                <span class="mx-1">of</span>
                <span class="font-medium">{mailQueuePage.totalElements}</span>
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

                {#each Array(mailQueuePage.totalPages) as _, i}
                    {#if i === pagination.currentPage ||
                    i === 0 ||
                    i === mailQueuePage.totalPages - 1 ||
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
                        disabled={pagination.currentPage === mailQueuePage.totalPages - 1}
                        on:click={() => changePage(pagination.currentPage + 1)}
                >
                    다음
                </button>

                <button
                        class="relative inline-flex items-center rounded-r-md px-2 py-2 ring-1 ring-inset focus:z-20 focus:outline-offset-0 disabled:text-gray-500/50"
                        disabled={pagination.currentPage === mailQueuePage.totalPages - 1}
                        on:click={() => changePage(mailQueuePage.totalPages - 1)}
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
        <!-- 빈 상태 표시 -->
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
            <h3 class="mt-4 text-lg font-medium text-black dark:text-white">조건의 맞는 자료가 없습니다.</h3>
        </div>
    {/if}
</div>

<!-- 모달 컴포넌트 추가 -->
<Modal
        autoclose
        bind:open={showModal}
        class="w-full"
        size="xl"
>
    {#if selectedMail}
        <div class="p-4">
            <h2 class="mb-4 text-2xl font-bold">메일 상세 정보</h2>
            <div class="mt-4">
                <JsonViewer json={escapeContent(selectedMail)} />
            </div>
            <div class="mt-4">
                {#if selectedMail.content && selectedMail.content.body}
                    <iframe
                            srcdoc={selectedMail.content.body}
                            class="w-full h-96 bg-white rounded-lg shadow-lg border border-gray-200 dark:border-gray-700"
                            title="Email Body Preview"
                    ></iframe>
                {:else}
                    <p class="text-gray-500 dark:text-gray-400">메일 본문이 없습니다.</p>
                {/if}
            </div>
        </div>
    {/if}
</Modal>