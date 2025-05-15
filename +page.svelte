<script context="module" lang="ts">
	const springApiBaseUrl = import.meta.env.VITE_SPRING_API_BASE_URL || 'http://localhost:8080/api/data';

	// 이메일 상태 타입 정의
	export type EmailStatus = 
		'QUEUED' | 'PROCESSING' | 'SENT' | 'FAILED' | 'RETRY' | 'CANCELLED' | 'SCHEDULED';

	// 이메일 상태 옵션
	export const statusOptions = [
		{ value: '', label: '모든 상태' },
		{ value: 'QUEUED', label: '대기 중' },
		{ value: 'PROCESSING', label: '처리 중' },
		{ value: 'SENT', label: '발송 완료' },
		{ value: 'FAILED', label: '실패' },
		{ value: 'RETRY', label: '재시도 예정' },
		{ value: 'CANCELLED', label: '취소됨' },
		{ value: 'SCHEDULED', label: '예약됨' }
	];

	// 파라미터 타입 정의
	export interface SearchParams {
		page: number;
		size: number;
		sort: string;
		status?: string;
		subject?: string;
		sender?: string;
	}
</script>

<script lang="ts">
	import { onMount } from 'svelte';
	
	interface Mail {
		id: number;
		sender: string;
		subject: string;
		status: EmailStatus;
		createdAt: string;
		sentAt: string;
		// 필요한 추가 필드
	}
	
	interface PageInfo {
		totalPages: number;
		totalElements: number;
		number: number; // 현재 페이지 (0-based)
		size: number;
		first: boolean;
		last: boolean;
	}
	
	// 검색 파라미터 및 상태 관리
	let searchParams: SearchParams = {
		page: 0,
		size: 10,
		sort: 'id,desc'
	};
	
	// 검색 필터
	let subjectSearch = '';
	let senderSearch = '';
	let statusFilter = '';
	let startDate = '';
	let endDate = '';
	let advancedSearch = false; // 고급 검색 패널 상태
	
	// 데이터 상태
	let mails: Mail[] = [];
	let pageInfo: PageInfo | null = null;
	let loading = false;
	let error: string | null = null;
	
	// 오늘 날짜를 YYYY-MM-DD 형식으로 반환
	function getTodayDate(): string {
		const today = new Date();
		return today.toISOString().split('T')[0];
	}
	
	// 날짜를 YYYY-MM-DD 형식으로 변환
	function formatDateForInput(date: Date): string {
		return date.toISOString().split('T')[0];
	}
	
	// 날짜를 ISO 형식으로 변환 (API 호출용)
	function formatDateForApi(dateString: string): string {
		if (!dateString) return '';
		// 시간을 포함한 ISO 문자열로 변환
		const date = new Date(dateString);
		
		// 시작일의 경우 00:00:00, 종료일의 경우 23:59:59 설정
		return date.toISOString();
	}
	
	// 고급 검색 패널 토글
	function toggleAdvancedSearch() {
		advancedSearch = !advancedSearch;
	}
	
	// 검색 초기화
	function resetSearch() {
		subjectSearch = '';
		senderSearch = '';
		statusFilter = '';
		startDate = '';
		endDate = '';
		searchParams.page = 0;
		loadData();
	}
	
	async function loadMailQueue(params: SearchParams): Promise<{mails: Mail[], pageInfo: PageInfo}> {
		// URL 쿼리 파라미터 구성
		const queryParams = new URLSearchParams();
		queryParams.append('page', params.page.toString());
		queryParams.append('size', params.size.toString());
		queryParams.append('sort', params.sort);
		
		// 기본 필터
		if (params.status) queryParams.append('status', params.status);
		if (params.subject) queryParams.append('subject', params.subject);
		if (params.sender) queryParams.append('sender', params.sender);
		
		// 날짜 범위 필터
		if (startDate) {
			queryParams.append('startDate', formatDateForApi(startDate + 'T00:00:00'));
		}
		
		if (endDate) {
			queryParams.append('endDate', formatDateForApi(endDate + 'T23:59:59'));
		}
		
		// 검색 요청 URL
		let url = `${springApiBaseUrl}/emailQueues`;
		
		// 날짜 범위만 있는 경우 range API 사용
		if (startDate && endDate && !params.status && !params.subject && !params.sender) {
			url = `${springApiBaseUrl}/email-queue/range`;
		}
		
		const response = await fetch(`${url}?${queryParams}`);
		const data = await response.json();
		
		if (response.ok) {
			// 표준 Spring Data REST 응답 vs 커스텀 API 응답 처리
			const emailsList = data._embedded?.emailQueues || data || [];
			
			// 페이지 정보
			let pageInfoData = {
				totalPages: data.page?.totalPages || 1,
				totalElements: data.page?.totalElements || emailsList.length,
				number: data.page?.number || 0,
				size: data.page?.size || params.size,
				first: data.page?.first || params.page === 0,
				last: data.page?.last || true
			};
			
			return {
				mails: emailsList,
				pageInfo: pageInfoData
			};
		} else {
			console.error('메일 큐 로딩 실패:', data.error || data.message || '알 수 없는 오류');
			throw new Error(data.error || data.message || '메일 큐를 로드하는데 실패했습니다');
		}
	}
	
	// 데이터 로드 함수
	async function loadData() {
		try {
			loading = true;
			error = null;
			
			// 검색 필드 적용
			const params = { ...searchParams };
			if (subjectSearch) params.subject = subjectSearch;
			if (senderSearch) params.sender = senderSearch;
			if (statusFilter) params.status = statusFilter;
			
			// 날짜 범위는 loadMailQueue 함수 내에서 처리
			
			const result = await loadMailQueue(params);
			mails = result.mails;
			pageInfo = result.pageInfo;
			
			// 검색 결과가 없는 경우 처리
			if (mails.length === 0 && (subjectSearch || senderSearch || statusFilter || startDate || endDate)) {
				console.log('검색 결과가 없습니다.');
			}
		} catch (err) {
			console.error('데이터 로드 오류:', err);
			error = err instanceof Error ? err.message : '데이터를 로드하는데 실패했습니다';
		} finally {
			loading = false;
		}
	}
	
	// 검색 실행
	function handleSearch() {
		searchParams.page = 0; // 검색 시 첫 페이지로 이동
		loadData();
	}
	
	// 페이지 이동
	function goToPage(newPage: number) {
		if (newPage >= 0 && (!pageInfo || newPage < pageInfo.totalPages)) {
			searchParams.page = newPage;
			loadData();
		}
	}
	
	// 날짜 포맷팅
	function formatDate(dateString: string | null): string {
		if (!dateString) return '-';
		return new Date(dateString).toLocaleString('ko-KR');
	}
	
	// 상태별 스타일 클래스
	function getStatusClass(status: EmailStatus): string {
		switch(status) {
			case 'QUEUED': return 'bg-blue-100 text-blue-800';
			case 'PROCESSING': return 'bg-yellow-100 text-yellow-800';
			case 'SENT': return 'bg-green-100 text-green-800';
			case 'FAILED': return 'bg-red-100 text-red-800';
			case 'RETRY': return 'bg-purple-100 text-purple-800';
			case 'CANCELLED': return 'bg-gray-100 text-gray-800';
			case 'SCHEDULED': return 'bg-indigo-100 text-indigo-800';
			default: return 'bg-gray-100 text-gray-800';
		}
	}
	
	// 상태 표시 텍스트
	function getStatusText(status: EmailStatus): string {
		const option = statusOptions.find(opt => opt.value === status);
		return option ? option.label : status;
	}
	
	onMount(() => {
		loadData();
	});
</script>

<div class="container mx-auto px-4 py-8">
	<h1 class="text-2xl font-bold mb-6">이메일 큐 관리</h1>
	
	<!-- 검색 필터 -->
	<div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 mb-6">
		<div class="grid grid-cols-1 md:grid-cols-3 gap-4">
			<!-- 제목 검색 -->
			<div>
				<label for="subject-search" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
					제목 검색
				</label>
				<input 
					type="text" 
					id="subject-search"
					bind:value={subjectSearch}
					placeholder="제목으로 검색..."
					class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
					on:keydown={(e) => e.key === 'Enter' && handleSearch()}
				/>
			</div>
			
			<!-- 발신자 검색 -->
			<div>
				<label for="sender-search" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
					발신자 검색
				</label>
				<input 
					type="text" 
					id="sender-search"
					bind:value={senderSearch}
					placeholder="발신자로 검색..."
					class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
					on:keydown={(e) => e.key === 'Enter' && handleSearch()}
				/>
			</div>
			
			<!-- 상태 필터 -->
			<div>
				<label for="status-filter" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
					상태 필터
				</label>
				<select 
					id="status-filter"
					bind:value={statusFilter}
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
				class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
			>
				검색
			</button>
		</div>
	</div>
	
	<!-- 데이터 테이블 -->
	<div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
		<!-- 작업 버튼 영역 -->
		<div class="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
			<div class="flex space-x-3">
				<button 
					on:click={deleteSelectedEmails}
					disabled={selectedEmails.size === 0 || loading}
					class="px-3 py-1 bg-red-600 text-white rounded-md text-sm hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
				>
					<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
					</svg>
					선택 삭제 ({selectedEmails.size})
				</button>
				
				<button 
					on:click={loadData}
					disabled={loading}
					class="px-3 py-1 bg-gray-200 text-gray-700 rounded-md text-sm hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
				>
					<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
					</svg>
					새로고침
				</button>
			</div>
			
			<div class="text-sm text-gray-600 dark:text-gray-300">
				{#if pageInfo}
					총 <span class="font-medium">{pageInfo.totalElements}</span>개 항목
				{/if}
			</div>
		</div>
		
		{#if loading}
			<div class="p-8 text-center">
				<div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-blue-600 border-r-transparent"></div>
				<p class="mt-2 text-gray-600 dark:text-gray-400">데이터 로딩 중...</p>
			</div>
		{:else if error}
			<div class="p-8 text-center text-red-600">
				<p>{error}</p>
				<button 
					on:click={loadData}
					class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
				>
					다시 시도
				</button>
			</div>
		{:else if mails.length === 0}
			<div class="p-8 text-center text-gray-600 dark:text-gray-400">
				<p>조회된 이메일이 없습니다.</p>
			</div>
		{:else}
			<div class="overflow-x-auto">
				<table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
					<thead class="bg-gray-50 dark:bg-gray-700">
						<tr>
							<th class="px-3 py-3 w-10">
								<input 
									type="checkbox" 
									bind:checked={selectAll}
									on:change={toggleSelectAll}
									class="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
								/>
							</th>
							<th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
								ID
							</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
								제목
							</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
								발신자
							</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
								상태
							</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
								생성일시
							</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
								발송일시
							</th>
							<th class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
								작업
							</th>
						</tr>
					</thead>
					<tbody class="bg-white divide-y divide-gray-200 dark:bg-gray-800 dark:divide-gray-700">
						{#each mails as mail}
							<tr class="hover:bg-gray-50 dark:hover:bg-gray-700">
								<td class="px-3 py-4 whitespace-nowrap">
									<input 
										type="checkbox" 
										checked={selectedEmails.has(mail.id)}
										on:change={(e) => toggleSelectEmail(mail.id, e.target.checked)}
										class="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
									/>
								</td>
								<td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
									{mail.id}
								</td>
								<td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white max-w-xs truncate">
									{mail.subject || '(제목 없음)'}
								</td>
								<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 max-w-xs truncate">
									{mail.sender || '-'}
								</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<span class={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusClass(mail.status)}`}>
										{getStatusText(mail.status)}
									</span>
								</td>
								<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
									{formatDate(mail.createdAt)}
								</td>
								<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
									{formatDate(mail.sentAt)}
								</td>
								<td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
									<button
										on:click={() => deleteMail(mail.id)}
										class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300 ml-3"
									>
										삭제
									</button>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
			
			<!-- 페이지네이션 -->
			<div class="px-6 py-4 flex flex-col sm:flex-row sm:items-center sm:justify-between border-t border-gray-200 dark:border-gray-700">
				<div class="flex items-center mb-4 sm:mb-0">
					<p class="text-sm text-gray-700 dark:text-gray-300">
						총 <span class="font-medium">{pageInfo?.totalElements || 0}</span> 항목 중 
						<span class="font-medium">{pageInfo ? pageInfo.number * pageInfo.size + 1 : 0}</span>
						-
						<span class="font-medium">
							{pageInfo ? Math.min((pageInfo.number + 1) * pageInfo.size, pageInfo.totalElements) : 0}
						</span>
						표시
					</p>
					
					<div class="ml-4">
						<label for="pageSize" class="sr-only">페이지 크기</label>
						<select
							id="pageSize"
							value={searchParams.size}
							on:change={(e) => changePageSize(parseInt(e.target.value))}
							class="h-8 py-0 pl-2 pr-8 border-gray-300 focus:ring-blue-500 focus:border-blue-500 text-sm rounded-md dark:bg-gray-700 dark:border-gray-600 dark:text-white"
						>
							<option value="10">10개씩</option>
							<option value="20">20개씩</option>
							<option value="50">50개씩</option>
							<option value="100">100개씩</option>
						</select>
					</div>
				</div>
				
				<div class="flex space-x-2 justify-center">
					<!-- 처음으로 버튼 -->
					<button 
						on:click={goToFirstPage}
						disabled={!pageInfo || pageInfo.first}
						class="px-2 py-1 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
						aria-label="첫 페이지로 이동"
					>
						<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
						</svg>
					</button>
					
					<!-- 이전 버튼 -->
					<button 
						on:click={() => goToPage(pageInfo?.number - 1 || 0)}
						disabled={!pageInfo || pageInfo.first}
						class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
						aria-label="이전 페이지로 이동"
					>
						이전
					</button>
					
					<!-- 페이지 번호 -->
					{#if pageInfo}
						{#each Array(Math.min(5, pageInfo.totalPages)) as _, i}
							{@const pageNum = Math.max(0, Math.min(
								pageInfo.totalPages - 5,
								pageInfo.number - 2
							)) + i}
							
							{#if pageNum < pageInfo.totalPages}
								<button 
									on:click={() => goToPage(pageNum)}
									class={`px-3 py-1 border rounded-md text-sm font-medium 
										${pageNum === pageInfo.number
										? 'bg-blue-600 text-white border-blue-600'
										: 'border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700'}`}
									aria-label={`${pageNum + 1} 페이지로 이동`}
									aria-current={pageNum === pageInfo.number ? 'page' : undefined}
								>
									{pageNum + 1}
								</button>
							{/if}
						{/each}
					{/if}
					
					<!-- 다음 버튼 -->
					<button 
						on:click={() => goToPage(pageInfo?.number + 1 || 0)}
						disabled={!pageInfo || pageInfo.last}
						class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
						aria-label="다음 페이지로 이동"
					>
						다음
					</button>
					
					<!-- 마지막으로 버튼 -->
					<button 
						on:click={goToLastPage}
						disabled={!pageInfo || pageInfo.last}
						class="px-2 py-1 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
						aria-label="마지막 페이지로 이동"
					>
						<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
						</svg>
					</button>
				</div>
			</div>
		{/if}
	</div>
</div>
