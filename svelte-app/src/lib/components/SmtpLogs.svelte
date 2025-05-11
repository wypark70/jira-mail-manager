<script lang="ts">
    import { onMount, onDestroy } from 'svelte';
    import { sseStore } from '$lib/stores/sseStore';
    
    interface LogMessage {
        timestamp: string;
        level: string;
        message: string;
    }

    let logs: LogMessage[] = [];
    let autoScroll = true;
    let logContainer: HTMLDivElement;

    function handleLog(event: CustomEvent) {
        logs = [...logs, event.detail].slice(-100); // 최근 100개만 유지
        
        if (autoScroll && logContainer) {
            setTimeout(() => {
                logContainer.scrollTop = logContainer.scrollHeight;
            }, 0);
        }
    }

    function handleScroll() {
        if (!logContainer) return;
        
        const { scrollTop, scrollHeight, clientHeight } = logContainer;
        // 스크롤이 하단에서 50px 이상 떨어지면 자동 스크롤 비활성화
        autoScroll = scrollHeight - scrollTop - clientHeight < 50;
    }

    onMount(() => {
        window.addEventListener('sse-log', handleLog as EventListener);
        sseStore.connect();
    });

    onDestroy(() => {
        window.removeEventListener('sse-log', handleLog as EventListener);
    });
</script>

<div class="flex flex-col h-full">
    <div class="flex justify-between items-center mb-2 px-4 py-2 bg-gray-100 dark:bg-gray-800 rounded-t">
        <h3 class="text-lg font-medium text-gray-800 dark:text-gray-100">SMTP 서버 로그</h3>
        <div class="flex items-center space-x-2">
            <label class="flex items-center space-x-2 text-sm text-gray-700 dark:text-gray-300">
                <input
                    type="checkbox"
                    bind:checked={autoScroll}
                    class="form-checkbox h-4 w-4 text-blue-600"
                />
                <span>자동 스크롤</span>
            </label>
        </div>
    </div>

    <div
        bind:this={logContainer}
        on:scroll={handleScroll}
        class="flex-1 overflow-auto bg-white dark:bg-gray-900 p-4 font-mono text-sm rounded-b border border-gray-200 dark:border-gray-700"
        style="max-height: 500px;"
    >
        {#each logs as log}
            <div class="mb-1 {log.level.toLowerCase() === 'error' ? 'text-red-500' : 'text-gray-700 dark:text-gray-300'}">
                <span class="text-gray-500 dark:text-gray-400">
                    {new Date(log.timestamp).toLocaleTimeString()}
                </span>
                <span class="mx-2">|</span>
                <span class="uppercase font-semibold w-16 inline-block">
                    {log.level}
                </span>
                <span class="mx-2">|</span>
                <span>{log.message}</span>
            </div>
        {/each}

        {#if logs.length === 0}
            <div class="text-gray-500 dark:text-gray-400 text-center py-4">
                로그가 없습니다.
            </div>
        {/if}
    </div>
</div>

<style>
    /* Firefox scrollbar */
    .overflow-auto {
        scrollbar-width: thin;
        scrollbar-color: rgba(156, 163, 175, 0.5) transparent;
    }

    /* Chrome/Safari scrollbar */
    .overflow-auto::-webkit-scrollbar {
        width: 8px;
    }

    .overflow-auto::-webkit-scrollbar-track {
        background: transparent;
    }

    .overflow-auto::-webkit-scrollbar-thumb {
        background-color: rgba(156, 163, 175, 0.5);
        border-radius: 4px;
    }
</style>