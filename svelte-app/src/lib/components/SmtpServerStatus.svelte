<script lang="ts">
    import { onMount } from 'svelte';
    import ConfirmModal from './ConfirmModal.svelte';
    
    const SMTP_API = '/api/smtp';
    
    // 상태 관리
    let serverStatus = $state({ running: false, status: '확인 중...' });
    let showConfirmModal = $state(false);
    let confirmTitle = $state('');
    let confirmMessage = $state('');
    let confirmCallback = $state<() => Promise<void>>();

    // 파생된 상태들
    let statusIconClass = $derived(serverStatus.running ? 'text-green-500' : 'text-red-500');
    let statusBgClass = $derived(serverStatus.running ? 'bg-green-50' : 'bg-red-50');
    let statusPingClass = $derived(`absolute inline-flex h-full w-full animate-ping rounded-full opacity-75 ${serverStatus.running ? 'bg-green-50' : 'bg-red-50'}`);
    let statusDotClass = $derived(`relative inline-flex h-3 w-3 rounded-full ${serverStatus.running ? 'bg-green-50' : 'bg-red-50'}`);

    function showConfirm(title: string, message: string, callback: () => Promise<void>) {
        confirmTitle = title;
        confirmMessage = message;
        confirmCallback = callback;
        showConfirmModal = true;
    }

    async function handleStartServer() {
        try {
            const response = await fetch(`${SMTP_API}/start`, {
                method: 'POST'
            });
            const message = await response.text();
            await loadServerStatus();
        } catch (error) {
            console.error('서버 시작 중 오류:', error);
        }
    }

    async function handleStopServer() {
        try {
            const response = await fetch(`${SMTP_API}/stop`, {
                method: 'POST'
            });
            const message = await response.text();
            await loadServerStatus();
        } catch (error) {
            console.error('서버 중지 중 오류:', error);
        }
    }

    function startServer() {
        showConfirm(
            '서버 시작',
            'SMTP 서버를 시작하시겠습니까?',
            handleStartServer
        );
    }

    function stopServer() {
        showConfirm(
            '서버 중지',
            'SMTP 서버를 중지하시겠습니까?',
            handleStopServer
        );
    }

    async function loadServerStatus() {
        try {
            const response = await fetch(`${SMTP_API}/status`);
            serverStatus = await response.json();
        } catch (error) {
            console.error('서버 상태를 불러오는 중 오류 발생:', error);
        }
    }

    // 컴포넌트가 마운트되면 서버 상태 로드
    onMount(loadServerStatus);
</script>

<!-- 모달 컴포넌트를 최상단에 배치 -->
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
        <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100">SMTP 서버</h3>
        <div class="flex items-center space-x-2">
            <span class="relative flex h-3 w-3">
                <span class={statusPingClass}/> 
                <span class={statusDotClass}/>
            </span>
            <span class="text-sm font-medium text-gray-600 dark:text-gray-300">
                {serverStatus.status}
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
                    {#if serverStatus.running}
                        <path 
                            stroke-linecap="round" 
                            stroke-linejoin="round" 
                            stroke-width="2" 
                            d="M5 13l4 4L19 7"
                        />
                    {:else}
                        <path 
                            stroke-linecap="round" 
                            stroke-linejoin="round" 
                            stroke-width="2" 
                            d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                    {/if}
                </svg>
            </div>
            <div>
                <p class="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {serverStatus.running ? '서버 실행 중' : '서버 중지됨'}
                </p>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                    {serverStatus.running ? '메일 발송이 가능합니다' : '메일 발송이 불가능합니다'}
                </p>
            </div>
        </div>

        <div class="flex space-x-3">
            <button 
                class={`inline-flex items-center rounded-md px-4 py-2 text-sm font-medium transition-colors
                    ${serverStatus.running ? 
                    'cursor-not-allowed bg-gray-100 text-gray-400 dark:bg-gray-700 dark:text-gray-500' : 
                    'bg-green-600 text-white hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 dark:hover:bg-green-500'}`}
                disabled={serverStatus.running}
                onclick={startServer}
            >
                <svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                시작
            </button>
            
            <button 
                class={`inline-flex items-center rounded-md px-4 py-2 text-sm font-medium transition-colors
                    ${!serverStatus.running ? 
                    'cursor-not-allowed bg-gray-100 text-gray-400 dark:bg-gray-700 dark:text-gray-500' : 
                    'bg-red-600 text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 dark:hover:bg-red-500'}`}
                disabled={!serverStatus.running}
                onclick={stopServer}
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
