<script lang="ts">
    import { Card } from 'flowbite-svelte';
    import { alertService } from '$lib/alerts';

    async function handleProcessEmailQueueBatch() {
        try {
            const response = await fetch(`/api/email-queue-batch/process`, {
                method: 'POST'
            });
            const { message } = await response.json();
            alertService.success(message);
        } catch (error) {
            console.error('배치 프로그램 오류:', error);
        }
    }

    async function handleRetryFailedEmail() {
        try {
            const response = await fetch(`/api/email-queue/retry`, {
                method: 'POST'
            });
            const { message } = await response.json();
            alertService.success(message);
        } catch (error) {
            console.error('실패한 이메일 재시도 오류:', error);
        }
    }
</script>
<div class="grid grid-cols-1 gap-6 md:grid-cols-2 dark:text-white">
    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg"
            href="/mail-queue"
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">메일 큐 관리</h3>
        <p>메일 큐를 확인하고 관리합니다.</p>
    </Card>

    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
            onclick={handleProcessEmailQueueBatch}
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">배치 실행</h3>
        <p>메일 보내는 배치 프로그램을 실행합니다.</p>
    </Card>

    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
            onclick={handleRetryFailedEmail}
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">실패한 이메일 재시도</h3>
        <p>발송 실패한 메일을 재시도 상태로 변경 합니다.</p>
    </Card>
</div>