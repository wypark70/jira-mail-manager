<script lang="ts">
    import { Card } from 'flowbite-svelte';
    import { alertService } from '$lib/alerts';
    import ConfirmModal from './ConfirmModal.svelte';

    const springApiBaseUrl = import.meta.env.VITE_SPRING_API_BASE_URL;

    function processEmailQueueBatch() {
        showConfirm(
            '배치 실행',
            '메일 보내는 배치 프로그램을 실행하시겠습니까?',
            handleProcessEmailQueueBatch
        );
    }

    async function handleProcessEmailQueueBatch() {
        try {
            const response = await fetch(`${springApiBaseUrl}/email-queue-batch/process`, {
                method: 'POST'
            });
            const { message } = await response.json();
            alertService.success(message);
        } catch (error) {
            console.error('배치 프로그램 오류:', error);
        }
    }

    function retryFailedEmail() {
        showConfirm(
            '재시도 상태로 변경',
            '발송 실패한 메일을 재시도 상태로 변경하시겠습니까?',
            handleRetryFailedEmail
        );
    }

    async function handleRetryFailedEmail() {
        try {
            const response = await fetch(`${springApiBaseUrl}/email-queue/retry`, {
                method: 'POST'
            });
            const { message } = await response.json();
            alertService.success(message);
        } catch (error) {
            console.error('실패한 이메일 재시도 오류:', error);
        }
    }

    function moveSentToHistory() {
        showConfirm(
            '이력 테이블로 이동',
            '발신 성공한 이메일을 이력 테이블로 이동처럼 처리하시겠습니까?',
            handleMoveSentToHistory
        );
    }

    async function handleMoveSentToHistory() {
        try {
            const response = await fetch(`${springApiBaseUrl}/email-queue/move-to-history/SENT`, {
                method: 'POST'
            });
            const message = await response.text();
            alertService.success(message);
        } catch (error) {
            console.error('이력 테이블로 이동 오류:', error);
        }
    }

    function moveFailedToHistory() {
        showConfirm(
            '이력 테이블로 이동',
            '발신 성공한 이메일을 이력 테이블로 이동처럼 처리하시겠습니까?',
            handleFailedSentToHistory
        );
    }

    async function handleFailedSentToHistory() {
        try {
            const response = await fetch(`${springApiBaseUrl}/email-queue/move-to-history/FAILED`, {
                method: 'POST'
            });
            const message = await response.text();
            alertService.success(message);
        } catch (error) {
            console.error('이력 테이블로 이동 오류:', error);
        }
    }

    function deleteAllHistory() {
        showConfirm(
            '이력 테이블 데이터 삭제',
            '이력 테이블 데이터를 삭제 처리하시겠습니까?',
            handleDeleteAllHistory
        );
    }

    async function handleDeleteAllHistory() {
        try {
            const response = await fetch(`${springApiBaseUrl}/email-history/delete-all`, {
                method: 'DELETE'
            });
            const message = await response.text();
            alertService.success(message);
        } catch (error) {
            console.error('이력 테이블 데이터 삭제 오류:', error);
        }
    }

    let showConfirmModal = $state(false);
    let confirmTitle = $state('');
    let confirmMessage = $state('');
    let confirmCallback = $state<() => Promise<void>>();

    function showConfirm(title: string, message: string, callback: () => Promise<void>) {
        confirmTitle = title;
        confirmMessage = message;
        confirmCallback = callback;
        showConfirmModal = true;
    }
</script>

<ConfirmModal
        message={confirmMessage}
        onCancel={() => showConfirmModal = false}
        onConfirm={async () => {
        if (confirmCallback) {
            await confirmCallback();
        }
        showConfirmModal = false;
    }}
        show={showConfirmModal}
        title={confirmTitle}
/>

<div class="grid grid-cols-1 gap-6 md:grid-cols-2 dark:text-white">
    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
            onclick={processEmailQueueBatch}
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">👉 배치 실행</h3>
        <p>메일 보내는 배치 프로그램을 실행합니다.</p>
    </Card>

    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
            onclick={retryFailedEmail}
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">👉 실패한 이메일 재시도</h3>
        <p>발송 실패한 메일을 재시도 상태로 변경 합니다.</p>
    </Card>

    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
            onclick={moveSentToHistory}
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">👉 발신한 이메일 이력 테이블로 이동</h3>
        <p>발신 성공한 이메일을 이력 테이블로 이동처리 합니다.</p>
    </Card>

    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
            onclick={moveFailedToHistory}
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">👉 실패한 이메일 이력 테이블로 이동</h3>
        <p>실패한 이메일을 이력 테이블로 이동처리 합니다.</p>
    </Card>

    <Card
            class="rounded-lg p-6 shadow-md transition-shadow hover:shadow-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
            onclick={deleteAllHistory}
            size="xl"
    >
        <h3 class="mb-2 text-lg font-semibold">👉 이력 테이블 데이터 삭제</h3>
        <p>이력 테이블 데이터를 삭제 처리합니다.</p>
    </Card>
</div>