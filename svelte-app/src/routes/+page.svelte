<script lang="ts">
    import EmailQueueBatchStatus from '$lib/components/EmailQueueBatchStatus.svelte';
    import SmtpInterceptorServerStatus from '$lib/components/SmtpInterceptorServerStatus.svelte';
    import EmailQueueStatistics from '$lib/components/EmailQueueStatistics.svelte';
    import EmailQueueActions from '$lib/components/EmailQueueActions.svelte';
    import MoveToHistoryBatchStatus from "$lib/components/MoveToHistoryBatchStatus.svelte";
</script>

<svelte:head>
    <title>Email Manager - Dashboard</title>
    <meta content="Email Manager - Dashboard" name="description"/>
</svelte:head>

<div class="container mx-auto px-4 py-8">
    <div class="grid grid-cols-1 gap-6 md:grid-cols-1 dark:text-white mb-8">
        <!-- SMTP 서버 상태 -->
        <SmtpInterceptorServerStatus/>
    </div>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-2 dark:text-white mb-8">
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
