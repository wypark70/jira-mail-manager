<script lang="ts">
    import { onMount } from 'svelte';
    import { fetchEmailQueueList, fetchStatusCounts, type EmailQueueParams, type EmailQueueResponse, type StatusCounts, EmailStatus } from '$lib/api/emailQueue';
    import { page } from '$app/stores';
    
    // 상태 필터 옵션
    const statusOptions = [
        { value: '', label: '모든 상태' },
        { value: EmailStatus.QUEUED, label: '대기 중' },
        { value: EmailStatus.PROCESSING, label: '처리 중' },
        { value: EmailStatus.SENT, label: '발송 완료' },
        { value: EmailStatus.FAILED, label: '실패' },
        { value: EmailStatus.RETRY, label: '재시도 예정' },
        { value: EmailStatus.CANCELLED, label: '취소됨' },
        { value: EmailStatus.SCHEDULED, label: '예약됨' }
    ];
    
    // 페이징 및 검색 파라미터
    let params: EmailQueueParams = {
        page: 0,
        size: 10,
        sortBy: 'createdAt',
        sortDir: 'desc'
    };
    
    // 검색어 입력
    let subjectSearch = '';
    let senderSearch = '';
    let statusFilter = '';
    
    // 페이지 데이터 및 상태
    let loading = true;
    let error: string | null = null;
    let emailQueueData: EmailQueueResponse | null = null;
    let statusCounts: StatusCounts = {};
    
    // 날짜 포맷팅 함수
    function formatDate(dateString: string | null): string {
        if (!dateString) return '-';
        return new Date(dateString).toLocaleString('ko-KR');
    }
    
    // 상태에 따른 배지 스타일 클래스
    function getStatusBadgeClass(status: EmailStatus): string {
        switch(status) {
            case EmailStatus.QUEUED:
                return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300';
            case EmailStatus.PROCESSING:
                return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300';
            case EmailStatus.SENT:
                return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
            case EmailStatus.FAILED:
                return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300';
            case EmailStatus.RETRY:
                return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-300';
            case EmailStatus.CANCELLED:
                return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
            case EmailStatus.SCHEDULED:
                return 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-300';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    }
    
    // 데이터 로딩 함수
    async function loadData() {
        loading = true;
        error = null;
        
        try {
            // 검색 파라미터 적용
            const searchParams: EmailQueueParams = {
                ...params
            };
            
            if (subjectSearch) searchParams.subject = subjectSearch;
            if (senderSearch) searchParams.sender = senderSearch;
            if (statusFilter) searchParams.status = statusFilter as EmailStatus;
            
            // 데이터 로딩
            emailQueueData = await fetchEmailQueueList(searchParams);
            statusCounts = await fetchStatusCounts();
        } catch (err) {
            console.error('이메일 큐 데이터 로딩 실패:', err);
            error = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다';
        } finally {
            loading = false;
        }
    }
    
    // 검색 실행
    function handleSearch() {
        params.page = 0; // 검색 시 첫 페이지로 이동
        loadData();
    }
    
    // 페이지 변경
    function changePage(newPage: number) {
        if (newPage >= 0 && (!emailQueueData || newPage < emailQueueData.totalPages)) {
            params.page = newPage;
            loadData();
        }
    }
    
    // 정렬 변경
    function changeSort(column: string) {
        if (params.sortBy === column) {
            // 같은 컬럼이면 정렬 방향 변경
            params.sortDir = params.sortDir === 'asc' ? 'desc' : 'asc';
        } else {
            // 다른 컬럼이면 해당 컬럼으로 정렬하고 기본 내림차순
            params.sortBy = column;
            params.sortDir = 'desc';
        }
        loadData();
    }
    
    // 상태 필터 변경
    function handleStatusFilterChange() {
        params.page = 0; // 필터 변경 시 첫 페이지로 이동
        loadData();
    }
    
    // 컴포넌트 마운트 시 데이터 로드
    onMount(() => {
        loadData();
    });
</script>

<div class="space-y-6">
    <!-- 상태별 카운트 카드 -->
    <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
        {#each statusOptions.filter(option => option.value !== '') as { value, label }}
            <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 border border-gray-200 dark:border-gray-700">
                <div class="flex justify-between items-center">
                    <div>
                        <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400">{label}</h3>
                        <p class="text-2xl font-semibold">{statusCounts[value] || 0}</p>
                    </div>
                    <div class={`rounded-full p-2 ${getStatusBadgeClass(value as EmailStatus)}`}>
                        <span class="w-4 h-4 block"></span>
                    </div>
                </div>
            </div>
        {/each}
    </div>
    
    <!-- 검색 및 필터링 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 border border-gray-200 dark:border-gray-700">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
                <label for="subject-search" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">제목 검색</label>
                <input 
                    type="text" 
                    id="subject-search"
                    placeholder="제목으로 검색..." 
                    bind:value={subjectSearch}
                    class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                    on:keydown={(e) => e.key === 'Enter' && handleSearch()}
                />
            </div>
            
            <div>
                <label for="sender-search" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">발신자 검색</label>
                <input 
                    type="text" 
                    id="sender-search"
                    placeholder="발신자로 검색..." 
                    bind:value={senderSearch}
                    class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                    on:keydown={(e) => e.key === 'Enter' && handleSearch()}
                />
            </div>
            
            <div>
                <label for="status-filter" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">상태 필터</label>
                <select 
                    id="status-filter"
                    bind:value={statusFilter}
                    on:change={handleStatusFilterChange}
                    class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                    {#each statusOptions as option}
                        <option value={option.value}>{option.label}</option>
                    {/each}
                </select>
            </div>
        </div>
        
        <div class="mt-4 flex justify-end">
            <button 
                on:click={handleSearch}
                class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 dark:bg-blue-700 dark:hover:bg-blue-800"
            >
                검색
            </button>
        </div>
    </div>
    
    <!-- 데이터 테이블 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden border border-gray-200 dark:border-gray-700">
        {#if loading}
            <div class="p-8 text-center">
                <div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-blue-600 border-r-transparent"></div>
                <p class="mt-2 text-gray-600 dark:text-gray-400">데이터 로딩 중...</p>
            </div>
        {:else if error}
            <div class="p-8 text-center">
                <p class="text-red-600 dark:text-red-400">{error}</p>
                <button 
                    on:click={loadData}
                    class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 dark:bg-blue-700 dark:hover:bg-blue-800"
                >
                    다시 시도
                </button>
            </div>
        {:else if emailQueueData && emailQueueData.content.length === 0}
            <div class="p-8 text-center">
                <p class="text-gray-600 dark:text-gray-400">조회된 이메일 큐 항목이 없습니다.</p>
            </div>
        {:else if emailQueueData}
            <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead class="bg-gray-50 dark:bg-gray-700">
                        <tr>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer" on:click={() => changeSort('id')}>
                                ID {#if params.sortBy === 'id'}<span>{params.sortDir === 'asc' ? '↑' : '↓'}</span>{/if}
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer" on:click={() => changeSort('subject')}>
                                제목 {#if params.sortBy === 'subject'}<span>{params.sortDir === 'asc' ? '↑' : '↓'}</span>{/if}
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer" on:click={() => changeSort('sender')}>
                                발신자 {#if params.sortBy === 'sender'}<span>{params.sortDir === 'asc' ? '↑' : '↓'}</span>{/if}
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer" on:click={() => changeSort('status')}>
                                상태 {#if params.sortBy === 'status'}<span>{params.sortDir === 'asc' ? '↑' : '↓'}</span>{/if}
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer" on:click={() => changeSort('createdAt')}>
                                생성일시 {#if params.sortBy === 'createdAt'}<span>{params.sortDir === 'asc' ? '↑' : '↓'}</span>{/if}
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider cursor-pointer" on:click={() => changeSort('sentAt')}>
                                발송일시 {#if params.sortBy === 'sentAt'}<span>{params.sortDir === 'asc' ? '↑' : '↓'}</span>{/if}
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200 dark:bg-gray-800 dark:divide-gray-700">
                        {#each emailQueueData.content as email}
                            <tr class="hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer">
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{email.id}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">
                                    {email.subject || '(제목 없음)'}
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{email.sender || '-'}</td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusBadgeClass(email.status)}`}>
                                        {statusOptions.find(option => option.value === email.status)?.label || email.status}
                                    </span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{formatDate(email.createdAt)}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{formatDate(email.sentAt)}</td>
                            </tr>
                        {/each}
                    </tbody>
                </table>
            </div>
            
            <!-- 페이지네이션 -->
            {#if emailQueueData.totalPages > 1}
                <div class="px-6 py-4 flex items-center justify-between border-t border-gray-200 dark:border-gray-700">
                    <div>
                        <p class="text-sm text-gray-700 dark:text-gray-300">
                            총 <span class="font-medium">{emailQueueData.totalElements}</span> 항목 중 
                            <span class="font-medium">{emailQueueData.number * emailQueueData.size + 1}</span>
                            -
                            <span class="font-medium">
                                {Math.min((emailQueueData.number + 1) * emailQueueData.size, emailQueueData.totalElements)}
                            </span>
                            표시
                        </p>
                    </div>
                    <div class="flex space-x-2">
                        <!-- 이전 버튼 -->
                        <button 
                            on:click={() => changePage(params.page! - 1)}
                            disabled={emailQueueData.first}
                            class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            이전
                        </button>
                        
                        <!-- 페이지 번호 -->
                        {#each Array(Math.min(5, emailQueueData.totalPages)) as _, i}
                            <!-- 현재 페이지 주변 5개 페이지만 표시 -->
                            {@const pageNum = Math.max(0, Math.min(
                                emailQueueData.totalPages - 5,
                                params.page! - 2
                            )) + i}
                            
                            {#if pageNum < emailQueueData.totalPages}
                                <button 
                                    on:click={() => changePage(pageNum)}
                                    class={`px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium 
                                        ${pageNum === params.page
                                        ? 'bg-blue-600 text-white border-blue-600'
                                        : 'text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700'}`}
                                >
                                    {pageNum + 1}
                                </button>
                            {/if}
                        {/each}
                        
                        <!-- 다음 버튼 -->
                        <button 
                            on:click={() => changePage(params.page! + 1)}
                            disabled={emailQueueData.last}
                            class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            다음
                        </button>
                    </div>
                </div>
            {/if}
        {/if}
    </div>
</div>
