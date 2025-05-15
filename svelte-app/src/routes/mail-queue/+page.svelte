<script context="module" lang="ts">
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
</script>

<script lang="ts">
    // 기존 코드 유지...

    // 검색 필터 상태 추가
    let searchFilters = {
        status: '',  // 상태 필터
        subject: ''  // 제목 검색어
    };

    // 상태 옵션 정의
    const statusOptions = [
        {value: '', label: '전체 상태'},
        {value: 'QUEUED', label: '대기 중'},
        {value: 'RETRY', label: '재전송 대기 중'},
        {value: 'SENT', label: '발송 완료'},
        {value: 'FAILED', label: '실패'}
    ];

    let pagination: PaginationState = {
        currentPage: 0,
        pageSize: 10,
        sortBy: 'createdAt',
        sortDirection: 'asc'
    };

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
<div class="container mx-auto px-4 py-8">
    <!-- 헤더 섹션 -->
    <div class="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 class="text-3xl font-bold text-gray-900">메일 큐</h1>
        <div class="flex flex-wrap items-center gap-3">
            <select
                    class="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                    on:change={changePageSize}
                    value={pagination.pageSize}
            >
                <option value="5">5개씩 보기</option>
                <option value="10">10개씩 보기</option>
                <option value="20">20개씩 보기</option>
                <option value="50">50개씩 보기</option>
            </select>

            <a
                    class="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-all hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                    href="/mail-queue/create"
            >
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path d="M12 4v16m8-8H4" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
                </svg>
                새 메일 추가
            </a>
        </div>
    </div>

    <!-- 검색 필터 UI 추가 (헤더 섹션 아래에 배치) -->
    <div class="mb-6 flex gap-4 rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
        <div class="flex flex-1 items-center gap-4">
            <select
                bind:value={searchFilters.status}
                class="w-48 rounded-lg border border-gray-300 px-3 py-2"
            >
                {#each statusOptions as option}
                    <option value={option.value}>{option.label}</option>
                {/each}
            </select>

            <input
                type="text"
                bind:value={searchFilters.subject}
                placeholder="제목으로 검색..."
                class="flex-1 rounded-lg border border-gray-300 px-3 py-2"
            />
        </div>

        <div class="flex items-center gap-2">
            <button
                on:click={applyFilters}
                class="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
            </svg>
                검색
            </button>

            <button
                on:click={resetFilters}
                class="inline-flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
                초기화
            </button>
        </div>
    </div>

    {#if mailQueuePage.content.length > 0}
        <!-- 테이블 섹션 -->
        <div class="mb-6 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
            <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                    <tr>
                        {#each ['id', 'sender', 'subject', 'status', 'createdAt'] as column}
                            <th
                                    class="group px-6 py-3.5 text-left text-sm font-semibold text-gray-900"
                                    on:click={() => changeSort(column)}
                            >
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
                            </th>
                        {/each}
                        <th class="px-6 py-3.5 text-right text-sm font-semibold text-gray-900">
                            액션
                        </th>
                    </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-200">
                    {#each mailQueuePage.content as mail}
                        <tr class="transition-colors hover:bg-gray-50">
                            <td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                                {mail.id}
                            </td>
                            <td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                                {mail.sender}
                            </td>
                            <td class="px-6 py-4 text-sm text-gray-900">
                                {mail.subject}
                            </td>
                            <td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                                {mail.status}
                            </td>
                            <td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                                {new Date(mail.createdAt).toLocaleString()}
                            </td>
                            <td class="whitespace-nowrap px-6 py-4 text-right text-sm">
                                <div class="flex items-center justify-end gap-3">
                                    <a
                                            href={`/mail-queue/edit/${mail.id}`}
                                            class="font-medium text-blue-600 transition-colors hover:text-blue-700"
                                    >
                                        수정
                                    </a>
                                    <button
                                            on:click={() => deleteMail(mail.id)}
                                            class="font-medium text-red-600 transition-colors hover:text-red-700"
                                    >
                                        삭제
                                    </button>
                                </div>
                            </td>
                        </tr>
                    {/each}
                    </tbody>
                </table>
            </div>
        </div>

        <!-- 페이지네이션 섹션 -->
        <div class="mt-6 flex flex-col items-center justify-between gap-4 sm:flex-row">
            <p class="text-sm text-gray-700">
                <span class="font-medium text-gray-900">
                    {pagination.currentPage * pagination.pageSize + 1}
                </span>
                -
                <span class="font-medium text-gray-900">
                    {Math.min((pagination.currentPage + 1) * pagination.pageSize, mailQueuePage.totalElements)}
                </span>
                <span class="mx-1">of</span>
                <span class="font-medium text-gray-900">{mailQueuePage.totalElements}</span>
                개 항목
            </p>

            <nav class="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                <button
                        class="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
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
                        class="relative inline-flex items-center px-3 py-2 text-sm font-medium text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
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
                                class="relative inline-flex items-center px-4 py-2 text-sm font-semibold {
                                pagination.currentPage === i
                                    ? 'z-10 bg-blue-600 text-white focus:z-20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600'
                                    : 'text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0'
                            }"
                                on:click={() => changePage(i)}
                                aria-current={pagination.currentPage === i ? 'page' : undefined}
                        >
                            {i + 1}
                        </button>
                    {:else if (i === pagination.currentPage - 2 || i === pagination.currentPage + 2)}
                        <span class="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-700 ring-1 ring-inset ring-gray-300 focus:outline-offset-0">
                            ...
                        </span>
                    {/if}
                {/each}

                <button
                        class="relative inline-flex items-center px-3 py-2 text-sm font-medium text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                        disabled={pagination.currentPage === mailQueuePage.totalPages - 1}
                        on:click={() => changePage(pagination.currentPage + 1)}
                >
                    다음
                </button>

                <button
                        class="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
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
            </nav>
        </div>

    {:else}
        <!-- 빈 상태 표시 -->
        <div class="rounded-lg border border-gray-200 bg-white p-12 text-center shadow-sm">
            <svg
                    class="mx-auto h-16 w-16 text-gray-400"
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
            <h3 class="mt-4 text-lg font-medium text-gray-900">메일 큐가 비어 있습니다</h3>
            <p class="mt-2 text-sm text-gray-500">새 메일을 추가하여 시작하세요.</p>
            <a
                    href="/mail-queue/create"
                    class="mt-6 inline-flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-all hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                </svg>
                새 메일 추가
            </a>
        </div>
    {/if}
</div>