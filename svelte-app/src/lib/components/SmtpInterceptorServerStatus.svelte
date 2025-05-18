<script lang="ts">
    import {onMount} from 'svelte';
    import ConfirmModal from './ConfirmModal.svelte';
    import {Button, Card} from 'flowbite-svelte';

    const SMTP_API = '/api/smtp';

    // 상태 관리
    let serverStatus = $state({running: false, status: '확인 중...'});
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

    // 주기적으로 상태 업데이트 (30초마다)
    onMount(() => {
        const interval = setInterval(loadServerStatus, 30000);
        return () => clearInterval(interval);
    });
</script>

<!-- 모달 컴포넌트를 최상단에 배치 -->
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
        <h3 class="text-lg font-semibold">SMTP Interceptor 서버</h3>
    </div>

    <div class="mt-6 flex items-center justify-between">
        <div class="flex items-center space-x-4">
            {#if serverStatus.running}
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
                        <path d="M9 10a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z" stroke-linecap="round"
                              stroke-linejoin="round"
                              stroke-width="2"/>
                    </svg>
                </div>
            {/if}
            <div>
                <p class="text-md font-semibold">
                    {serverStatus.running ? '서버 실행 중' : '서버 중지됨'}
                </p>
                <p class="text-sm">
                    {serverStatus.running ? 'SMTP Interceptor 서버가 실행 되었습니다.' : 'SMTP Interceptor 서버가 중지 되었습니다.'}
                </p>
            </div>
        </div>

        <div class="flex space-x-3">
            <Button
                    color="green"
                    disabled={serverStatus.running}
                    onclick={startServer}
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
                    disabled={!serverStatus.running}
                    onclick={stopServer}
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
