<script lang="ts">
	import EmailQueueBatchProcessorStatus from '$lib/components/EmailQueueBatchProcessorStatus.svelte';
	import SmtpInterceptorServerStatus from '$lib/components/SmtpInterceptorServerStatus.svelte';
	import EmailQueueStatistics from '$lib/components/EmailQueueStatistics.svelte';
	import EmailQueueActions from '$lib/components/EmailQueueActions.svelte';

	// 대시보드에 필요한 데이터 (실제 데이터와 연동 필요)
	let stats = {
		totalMails: 150,
		pendingMails: 45,
		sentMails: 105,
		failedMails: 0
	};
</script>

<svelte:head>
	<title>Email Manager - Dashboard</title>
	<meta name="description" content="Email Manager - Dashboard" />
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- SMTP 서버 상태 -->
	<div class="mb-8">
		<SmtpInterceptorServerStatus />
	</div>

	<!-- 배치 프로세서 상태 -->
	<div class="mb-8">
		<EmailQueueBatchProcessorStatus />
	</div>

	<!-- 통계 카드 -->
	<div class="mb-8">
		<EmailQueueStatistics />
	</div>

	<!-- 빠른 액션 버튼 -->
	<div class="mb-8">
		<EmailQueueActions />
	</div>
</div>
