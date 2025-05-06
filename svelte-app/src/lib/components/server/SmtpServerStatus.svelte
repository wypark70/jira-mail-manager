<script>
    import { onMount } from 'svelte';

    const SMTP_API = '/api/smtp';

    let statusText = '로딩 중...';
    let isRunning = false;

    // 컴포넌트가 마운트되면 서버 상태 로드
    onMount(() => {
        loadServerStatus();
    });

    async function startServer() {
        try {
            const response = await fetch(`${SMTP_API}/start`, {
                method: 'POST'
            });
            const message = await response.text();
            alert(message);
            loadServerStatus();
        } catch (error) {
            alert('오류가 발생했습니다: ' + error);
        }
    }

    async function stopServer() {
        try {
            const response = await fetch(`${SMTP_API}/stop`, {
                method: 'POST'
            });
            const message = await response.text();
            alert(message);
            loadServerStatus();
        } catch (error) {
            alert('오류가 발생했습니다: ' + error);
        }
    }

    async function loadServerStatus() {
        try {
            const response = await fetch(`${SMTP_API}/status`);
            const data = await response.json();

            isRunning = data.running;
            statusText = data.status;
        } catch (error) {
            console.error('서버 상태를 불러오는 중 오류 발생:', error);
        }
    }
</script>

<div class="status-box" class:running={isRunning} class:stopped={!isRunning}>
    <h2>서버 상태: <span>{statusText}</span></h2>
</div>

<div class="button-container">
    <button
            class="button start-button"
            disabled={isRunning}
            class:disabled={isRunning}
            on:click={startServer}>
        시작
    </button>

    <button
            class="button stop-button"
            disabled={!isRunning}
            class:disabled={!isRunning}
            on:click={stopServer}>
        중지
    </button>
</div>

<style>
    .status-box {
        padding: 15px;
        margin-bottom: 20px;
        border-radius: 4px;
    }

    .running {
        background-color: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
    }

    .stopped {
        background-color: #f8d7da;
        color: #721c24;
        border: 1px solid #f5c6cb;
    }

    .button-container {
        display: flex;
        gap: 10px;
        flex-direction: row;
        flex-wrap: nowrap;
        justify-content: flex-end;
        align-items: center;
    }

    .button {
        padding: 10px 20px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 16px;
    }

    .start-button {
        background-color: #28a745;
        color: white;
    }

    .stop-button {
        background-color: #dc3545;
        color: white;
    }

    .disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }
</style>