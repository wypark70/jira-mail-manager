<script lang="ts">
    import {onMount} from 'svelte';
    import ConfirmModal from './ConfirmModal.svelte';
    import {Button, Card} from 'flowbite-svelte';
    import {alertService} from "$lib/alerts";

    const PROCESSOR_API = '/api/move-to-history-batch';

    // 상태 관리
    let processorStatus = $state({
        running: false,
        processorId: null,
        startedAt: null,
        status: '확인 중...'
    });
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

    async function handleStartProcessor() {
        try {
            const response = await fetch(`${PROCESSOR_API}/start`, {
                method: 'POST'
            });
            const result = await response.json();
            if (result.success) {
                alertService.success(result.message);
                await loadProcessorStatus();
            } else {
                alertService.error(result.message);
            }
        } catch (error) {
            console.error('히스토리 테이블 이동 배치 시작 중 오류:', error);
        }
    }

    async function handleStopProcessor() {
        try {
            const response = await fetch(`${PROCESSOR_API}/stop`, {
                method: 'POST'
            });
            const result = await response.json();
            if (result.success) {
                alertService.success(result.message);
                await loadProcessorStatus();
            } else {
                alertService.error(result.message);
            }
        } catch (error) {
            console.error('히스토리 테이블 이동 배치 중지 중 오류:', error);
        }
    }

    function startProcessor() {
        showConfirm(
            '히스토리 테이블 이동 배치 시작',
            '히스토리 테이블 이동 배치를 시작하시겠습니까?',
            handleStartProcessor
        );
    }

    function stopProcessor() {
        showConfirm(
            '히스토리 테이블 이동 배치 중지',
            '히스토리 테이블 이동 배치를 중지하시겠습니까?',
            handleStopProcessor
        );
    }

    async function loadProcessorStatus() {
        try {
            const response = await fetch(`${PROCESSOR_API}/status`);
            processorStatus = await response.json();
        } catch (error) {
            console.error('히스토리 테이블 이동 배치 상태를 불러오는 중 오류 발생:', error);
        }
    }

    // 컴포넌트가 마운트되면 프로세서 상태 로드
    onMount(loadProcessorStatus);

    // 주기적으로 상태 업데이트 (30초마다)
    onMount(() => {
        const interval = setInterval(loadProcessorStatus, 30000);
        return () => clearInterval(interval);
    });
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

<Card class="rounded-lg p-6 shadow-lg dark:text-white" size="xl">
    <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-semibold">히스토리 테이블 이동 배치</h3>
    </div>

    <div class="mt-6 flex items-center justify-between">
        <div class="flex items-center space-x-4">

            {#if processorStatus.running}
                <div class="rounded-full p-3 bg-green-500 text-white">
                    <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"
                              stroke-linecap="round" stroke-linejoin="round"
                              stroke-width="2"/>
                        <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round" stroke-linejoin="round"
                              stroke-width="2"/>
                    </svg>
                </div>
            {:else}
                <div class="rounded-full p-3 bg-orange-500 text-white">
                    <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round" stroke-linejoin="round"
                              stroke-width="2"/>
                        <path d="M9 10a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"
                              stroke-linecap="round"
                              stroke-linejoin="round"
                              stroke-width="2"/>
                    </svg>
                </div>
            {/if}
            <div>
                <p class="text-md font-semibold">
                    {processorStatus.running ? '실행 중' : '중지됨'}
                </p>
                <p class="text-sm">
                    {#if processorStatus.running && processorStatus.startedAt}
                        시작 시간: {new Date(processorStatus.startedAt).toLocaleString()}
                    {:else}
                        히스토리 테이블 이동 배치 처리가 중지되었습니다
                    {/if}
                </p>
            </div>
        </div>

        <div class="flex space-x-3 whitespace-nowrap">
            <Button
                    color="green"
                    disabled={processorStatus.running}
                    onclick={startProcessor}
            >
                <svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"
                          stroke-linecap="round" stroke-linejoin="round"
                          stroke-width="2"/>
                    <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round" stroke-linejoin="round"
                          stroke-width="2"/>
                </svg>
                시작
            </Button>

            <Button
                    color="orange"
                    disabled={!processorStatus.running}
                    onclick={stopProcessor}
                    class="whitespace-nowrap"
            >
                <svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round" stroke-linejoin="round"
                          stroke-width="2"/>
                    <path d="M9 10a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z" stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"/>
                </svg>
                중지
            </Button>
        </div>
    </div>
</Card>