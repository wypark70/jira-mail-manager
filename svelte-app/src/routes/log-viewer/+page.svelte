<script lang="ts">
    import {Button, Listgroup, ListgroupItem, Range} from "flowbite-svelte";
    import {onDestroy, onMount} from 'svelte';
    import {sseConnection} from '$lib/stores/sseConnectionStore';
    import type {LogMessage} from '$lib/types/index.ts';
    import {Buffer} from '$lib/utils/buffer';

    let maxLogs = 1000; // 기본값
    const RENDER_INTERVAL = 1000; // ms

    let logs: LogMessage[] = [];
    let logBuffer: Buffer<LogMessage>;
    let renderInterval: number;

    $: {
        // maxLogs가 변경될 때마다 새로운 버퍼 생성
        logBuffer = new Buffer<LogMessage>(maxLogs);
        // 기존 로그도 새로운 최대값에 맞게 조정
        logs = logs.slice(-maxLogs);
    }

    $: connectionStatus = $sseConnection.isConnected ? '연결됨' : '연결 끊김';
    $: connectionError = $sseConnection.error;

    function setupEventSource() {
        const es = sseConnection.connect(); // lastEventId 전달
        if (!es) return;

        es.addEventListener('message', (event) => {
            try {
                const log = JSON.parse(event.data);
                logBuffer.add(log);
            } catch (error) {
                console.error('로그 파싱 오류:', error);
            }
        });
    }


    function startRendering() {
        renderInterval = window.setInterval(() => {
            const newLogs = logBuffer.flush();
            if (newLogs.length > 0) {
                logs = [...logs, ...newLogs].slice(-maxLogs);
            }
        }, RENDER_INTERVAL);
    }

    function getColorByLogLevel(level: string) {
        if (level === 'ERROR') return 'text-red-500';
        if (level === 'WARN') return 'text-yellow-500';
        return 'text-green-500';
    }

    onMount(() => {
        setupEventSource();
        startRendering();
    });

    onDestroy(() => {
        sseConnection.disconnect();
        clearInterval(renderInterval);
    });
</script>


<div class="container mx-auto px-4 py-8">
    <div class="mb-8">
        <div class="mb-6 flex gap-4 rounded-lg border border-gray-200 dark:border-gray-700 p-4 shadow-sm dark:text-white">
            <div class="flex flex-1 items-center gap-4">
                <div class={`w-8 h-8 rounded-full ${$sseConnection.isConnected ? 'bg-green-500' : 'bg-red-500'}`}></div>
                <span>{connectionStatus}</span>
                <Range bind:value={maxLogs} class="w-70" max="5000" min="100" step="100"/>
                <span class="text-sm ml-2 min-w-[4rem]">{maxLogs.toLocaleString()}</span>
            </div>
            <div class="text-red-500 text-sm">
                {connectionError}
                <Button color="green" onclick={() => setupEventSource()}>재연결</Button>
            </div>
        </div>

        <Listgroup active class="w-full">
            {#each logs as log}
                <ListgroupItem class="flex items-start gap-2 font-semibold">
                    <span class="whitespace-nowrap text-orange-400">{new Date(log.timestamp).toLocaleTimeString()}</span>
                    <span class="whitespace-nowrap {getColorByLogLevel(log.level)}">{log.level}</span>
                    <span id="log-message" class="break-all">{log.message}</span>
                </ListgroupItem>
            {/each}
        </Listgroup>
    </div>
</div>
