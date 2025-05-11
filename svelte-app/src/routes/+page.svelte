<script lang="ts">
	import BatchProcessorStatus from '$lib/components/BatchProcessorStatus.svelte';
	import SmtpServerStatus from '$lib/components/SmtpServerStatus.svelte';
	import StatisticsCards from '$lib/components/StatisticsCards.svelte';

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

	<!-- 배치 프로세서 상태 -->
	<div class="mb-8">
		<BatchProcessorStatus />
	</div>

	<!-- 통계 카드 -->
	<div class="mb-8">
		<StatisticsCards />
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
