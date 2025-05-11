<script lang="ts">
	import SmtpServerStatus from '$lib/components/SmtpServerStatus.svelte';

	// 대시보드에 필요한 데이터 (실제 데이터와 연동 필요)
	let stats = {
		totalMails: 150,
		pendingMails: 45,
		sentMails: 105,
		failedMails: 0
	};
</script>

<svelte:head>
	<title>메일 관리자 대시보드</title>
	<meta name="description" content="메일 관리자 대시보드" />
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- SMTP 서버 상태 -->
	<div class="mb-8">
		<SmtpServerStatus />
	</div>

	<!-- 통계 카드 -->
	<div class="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
		<div class="rounded-lg bg-white p-6 shadow-md">
			<div class="flex items-center">
				<div class="rounded-full bg-blue-100 p-3 text-blue-500">
					<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
						/>
					</svg>
				</div>
				<div class="ml-4">
					<h2 class="text-sm text-gray-600">총 메일</h2>
					<p class="text-2xl font-semibold text-gray-800">{stats.totalMails}</p>
				</div>
			</div>
		</div>

		<div class="rounded-lg bg-white p-6 shadow-md">
			<div class="flex items-center">
				<div class="rounded-full bg-yellow-100 p-3 text-yellow-500">
					<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
						/>
					</svg>
				</div>
				<div class="ml-4">
					<h2 class="text-sm text-gray-600">대기 중</h2>
					<p class="text-2xl font-semibold text-gray-800">{stats.pendingMails}</p>
				</div>
			</div>
		</div>

		<div class="rounded-lg bg-white p-6 shadow-md">
			<div class="flex items-center">
				<div class="rounded-full bg-green-100 p-3 text-green-500">
					<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M5 13l4 4L19 7"
						/>
					</svg>
				</div>
				<div class="ml-4">
					<h2 class="text-sm text-gray-600">발송 완료</h2>
					<p class="text-2xl font-semibold text-gray-800">{stats.sentMails}</p>
				</div>
			</div>
		</div>

		<div class="rounded-lg bg-white p-6 shadow-md">
			<div class="flex items-center">
				<div class="rounded-full bg-red-100 p-3 text-red-500">
					<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
						/>
					</svg>
				</div>
				<div class="ml-4">
					<h2 class="text-sm text-gray-600">실패</h2>
					<p class="text-2xl font-semibold text-gray-800">{stats.failedMails}</p>
				</div>
			</div>
		</div>
	</div>

	<!-- 빠른 액션 버튼 -->
	<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
		<a
			href="/mail-queue"
			class="rounded-lg bg-white p-6 shadow-md transition-shadow hover:shadow-lg"
		>
			<h3 class="mb-2 text-lg font-semibold text-gray-800">메일 큐 관리</h3>
			<p class="text-gray-600">메일 큐를 확인하고 관리합니다.</p>
		</a>

		<a
			href="/mail-queue/create"
			class="rounded-lg bg-white p-6 shadow-md transition-shadow hover:shadow-lg"
		>
			<h3 class="mb-2 text-lg font-semibold text-gray-800">새 메일 작성</h3>
			<p class="text-gray-600">새로운 메일을 작성하고 예약합니다.</p>
		</a>
	</div>
</div>
