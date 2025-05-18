<script lang="ts">
    import {
        Button,
        ButtonGroup,
        Card,
        Input,
        Select,
        Table,
        TableBody,
        TableBodyCell,
        TableBodyRow,
        TableHead,
        TableHeadCell
    } from "flowbite-svelte";
    import {page} from '$app/state';

    const springApiBaseUrl = 'http://localhost:8080/api';

    interface PageResponse<T> {
        content: T[];
        totalPages: number;
        totalElements: number;
        currentPage: number;
    }

    interface Mail {
        id: number;
        recipient: string;
        sender: string;
        status: string;
        subject: string;
        createdAt: string;
        sentAt: string | null;
    }

    interface PaginationState {
        currentPage: number;
        pageSize: number;
        sortBy: string;
        sortDirection: string;
    }

    // 검색 필터 상태 추가
    let searchFilters = {
        status: page.url.searchParams.get('status') || '',  // 상태 필터
        subject: ''  // 제목 검색어
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
        sortDirection: 'asc'
    };

    const pageSizeOptions = [
        {value: 5, label: '5개씩 보기'},
        {value: 10, label: '10개씩 보기'},
        {value: 20, label: '20개씩 보기'},
        {value: 50, label: '50개씩 보기'}
    ];

    let mailQueuePage: PageResponse<Mail> = {
        content: [],
        totalPages: 0,
        totalElements: 0,
        currentPage: 0
    };

    // loadMailQueue 함수 수정
    async function loadMailQueue(): Promise<void> {
        const {currentPage, pageSize, sortBy, sortDirection} = pagination;
        const {status, subject} = searchFilters;

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

        const response = await fetch(`${springApiBaseUrl}/email-queue/search?${params}`);
        if (response.ok) {
            mailQueuePage = await response.json();
        } else {
            console.error('메일 큐 로딩 실패:', await response.json());
        }
    }

    // 필터 적용 함수
    async function applyFilters(): Promise<void> {
        pagination.currentPage = 0;  // 필터 적용시 첫 페이지로 이동
        await loadMailQueue();
    }

    // 필터 초기화 함수
    async function resetFilters(): Promise<void> {
        searchFilters.status = '';
        searchFilters.subject = '';
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

    async function deleteMail(id: number): Promise<void> {
        if (confirm('정말로 삭제하시겠습니까?')) {
            const response = await fetch(`${springApiBaseUrl}/emailQueues/${id}`, {
                method: 'DELETE'
            });
            if (response.ok) {
                await loadMailQueue(); // 삭제 후 현재 페이지 새로고침
            } else {
                const data = await response.json();
                console.error('메일 삭제 실패:', data.error);
                alert('메일 삭제에 실패했습니다.');
            }
        }
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

    <!-- 검색 필터 UI 추가 (헤더 섹션 아래에 배치) -->
    <div class="mb-6 flex gap-4 rounded-lg border border-gray-200 dark:border-gray-700 p-4 shadow-sm dark:text-white">
        <div class="flex flex-1 items-center gap-4">
            <Select
                    bind:value={searchFilters.status}
                    onchange={applyFilters}
            >
                {#each statusOptions as option}
                    <option value={option.value}>{option.label}</option>
                {/each}
            </Select>

            <Input
                    bind:value={searchFilters.subject}
                    onchange={applyFilters}
                    placeholder="제목으로 검색..."
                    type="text"
            />
        </div>

        <ButtonGroup>
            <Button onclick={applyFilters} class="dark:text-white">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke-linecap="round" stroke-linejoin="round"
                          stroke-width="2"/>
                </svg>
                검색
            </Button>
            <Button onclick={resetFilters} class="dark:text-white">
                초기화
            </Button>
        </ButtonGroup>
    </div>

    {#if mailQueuePage.content.length > 0}
        <!-- 테이블 섹션 -->
        <Card size="xl">
            <Table>
                <TableHead class="dark:text-white">
                    {#each ['id', 'sender', 'subject', 'status', 'createdAt'] as column}
                        <TableHeadCell onclick={() => changeSort(column)}>
                            <div class="flex cursor-pointer items-center gap-2 hover:text-blue-600">
                                {column}
                                {#if pagination.sortBy === column}
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
                        <TableBodyRow>
                            <TableBodyCell>{mail.id}</TableBodyCell>
                            <TableBodyCell>{mail.sender}</TableBodyCell>
                            <TableBodyCell>{mail.subject}</TableBodyCell>
                            <TableBodyCell>{mail.status}</TableBodyCell>
                            <TableBodyCell>{new Date(mail.createdAt).toLocaleString()}</TableBodyCell>
                        </TableBodyRow>
                    {/each}
                </TableBody>
            </Table>
        </Card>
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
                </Select></nav>
        </div>

    {:else}
        <!-- 빈 상태 표시 -->
        <div class="rounded-lg border p-12 text-center shadow-sm text-gray-200">
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
