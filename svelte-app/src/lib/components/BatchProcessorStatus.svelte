<script lang="ts">
    import { onMount } from 'svelte';
    import ConfirmModal from './ConfirmModal.svelte';
    
    const PROCESSOR_API = '/api/email-queue/processor';
    
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

    // 파생된 상태들
    let statusIconClass = $derived(processorStatus.running ? 'text-blue-500' : 'text-gray-500');
    let statusBgClass = $derived(processorStatus.running ? 'bg-blue-50' : 'bg-gray-50');
    let statusPingClass = $derived(`absolute inline-flex h-full w-full animate-ping rounded-full opacity-75 ${processorStatus.running ? 'bg-blue-50' : 'bg-gray-50'}`);
    let statusDotClass = $derived(`relative inline-flex h-3 w-3 rounded-full ${processorStatus.running ? 'bg-blue-50' : 'bg-gray-50'}`);

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
                await loadProcessorStatus();
            } else {
                console.error('프로세서 시작 실패:', result.message);
            }
        } catch (error) {
            console.error('프로세서 시작 중 오류:', error);
        }
    }

    async function handleStopProcessor() {
        try {
            const response = await fetch(`${PROCESSOR_API}/stop`, {
                method: 'POST'
            });
            const result = await response.json();
            if (result.success) {
                await loadProcessorStatus();
            } else {
                console.error('프로세서 중지 실패:', result.message);
            }
        } catch (error) {
            console.error('프로세서 중지 중 오류:', error);
        }
    }

    function startProcessor() {
        showConfirm(
            '프로세서 시작',
            '이메일 배치 프로세서를 시작하시겠습니까?',
            handleStartProcessor
        );
    }

    function stopProcessor() {
        showConfirm(
            '프로세서 중지',
            '이메일 배치 프로세서를 중지하시겠습니까?',
            handleStopProcessor
        );
    }

    async function loadProcessorStatus() {
        try {
            const response = await fetch(`${PROCESSOR_API}/status`);
            processorStatus = await response.json();
        } catch (error) {
            console.error('프로세서 상태를 불러오는 중 오류 발생:', error);
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
    show={showConfirmModal}
    title={confirmTitle}
    message={confirmMessage}
    onConfirm={async () => {
        if (confirmCallback) {
            await confirmCallback();
        }
        showConfirmModal = false;
    }}
    onCancel={() => showConfirmModal = false}
/>

<div class="rounded-lg bg-white p-6 shadow-lg dark:bg-gray-800">
    <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100">이메일 배치 프로세서</h3>
        <div class="flex items-center space-x-2">
            <span class="relative flex h-3 w-3">
                <span class={statusPingClass}/> 
                <span class={statusDotClass}/>
            </span>
            <span class="text-sm font-medium text-gray-600 dark:text-gray-300">
                {processorStatus.status}
            </span>
        </div>
    </div>

    <div class="mt-6 flex items-center justify-between">
        <div class="flex items-center space-x-4">
            <div class={`rounded-full p-3 ${statusBgClass}`}>
                <svg 
                    class={`h-6 w-6 ${statusIconClass}`} 
                    fill="none" 
                    stroke="currentColor" 
                    viewBox="0 0 24 24"
                >
                    {#if processorStatus.running}
                        <path 
                            stroke-linecap="round" 
                            stroke-linejoin="round" 
                            stroke-width="2" 
                            d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"
                        />
                    {:else}
                        <path 
                            stroke-linecap="round" 
                            stroke-linejoin="round" 
                            stroke-width="2" 
                            d="M10 9v6m4-6v6m7-3a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                    {/if}
                </svg>
            </div>
            <div>
                <p class="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {processorStatus.running ? '프로세서 실행 중' : '프로세서 중지됨'}
                </p>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                    {#if processorStatus.running && processorStatus.startedAt}
                        시작 시간: {new Date(processorStatus.startedAt).toLocaleString()}
                    {:else}
                        자동 이메일 처리가 중지되었습니다
                    {/if}
                </p>
            </div>
        </div>

        <div class="flex space-x-3">
            <button 
                class={`inline-flex items-center rounded-md px-4 py-2 text-sm font-medium transition-colors
                    ${processorStatus.running ? 
                    'cursor-not-allowed bg-gray-100 text-gray-400 dark:bg-gray-700 dark:text-gray-500' : 
                    'bg-blue-600 text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 dark:hover:bg-blue-500'}`}
                disabled={processorStatus.running}
                on:click={startProcessor}
            >
                <svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                시작
            </button>
            
            <button 
                class={`inline-flex items-center rounded-md px-4 py-2 text-sm font-medium transition-colors
                    ${!processorStatus.running ? 
                    'cursor-not-allowed bg-gray-100 text-gray-400 dark:bg-gray-700 dark:text-gray-500' : 
                    'bg-red-600 text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 dark:hover:bg-red-500'}`}
                disabled={!processorStatus.running}
                on:click={stopProcessor}
            >
                <svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 10a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z" />
                </svg>
                중지
            </button>
        </div>
    </div>
</div>