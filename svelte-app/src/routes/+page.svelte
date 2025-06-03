<script lang="ts">
    import EmailQueueBatchStatus from '$lib/components/EmailQueueBatchStatus.svelte';
    import SmtpInterceptorServerStatus from '$lib/components/SmtpInterceptorServerStatus.svelte';
    import EmailQueueStatistics from '$lib/components/EmailQueueStatistics.svelte';
    import EmailQueueActions from '$lib/components/EmailQueueActions.svelte';
    import MoveToHistoryBatchStatus from "$lib/components/MoveToHistoryBatchStatus.svelte";

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
    <meta content="Email Manager - Dashboard" name="description"/>
</svelte:head>

<div class="container mx-auto px-4 py-8">
    <div class="grid grid-cols-1 gap-6 md:grid-cols-1 dark:text-white mb-8">
        <!-- SMTP 서버 상태 -->
        <SmtpInterceptorServerStatus/>
        <!-- 매일 발송 배치 상태 -->
        <EmailQueueBatchStatus/>
        <!-- 히스토리 테이블 이동 배치 상태 -->
        <MoveToHistoryBatchStatus/>
    </div>

    <!-- 통계 카드 -->
    <div class="mb-8">
        <EmailQueueStatistics/>
    </div>

    <!-- 빠른 액션 버튼 -->
    <div class="mb-8">
        <EmailQueueActions/>
    </div>
</div>
