<script lang="ts">
    import { onMount, onDestroy } from 'svelte';

    interface Stats {
        queuedCount: number;
        processingCount: number;
        sentCount: number;
        failedCount: number;
        retryCount: number;
        scheduledCount: number;
        totalCount: number;       
    };

    let stats = $state<Stats>({
        queuedCount: 0,
        processingCount: 0,
        sentCount: 0,
        failedCount: 0,
        retryCount: 0,
        scheduledCount: 0,
        totalCount: 0
    });

    const MAX_RETRIES = 5;
    let retryCount = 0;
    let eventSource: EventSource | null = null;
    let isConnected = false;

    function connectSSE() {
        try {
            eventSource = new EventSource('/api/email-queue/stats/stream');
            
            eventSource.onmessage = (event) => {
                stats = JSON.parse(event.data);
                isConnected = true;
            };

            eventSource.onerror = (error) => {
                console.error("실시간 통계 연결 오류:", error);
                isConnected = false;
                closeConnection();

                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    console.log(`재연결 시도 ${retryCount}/${MAX_RETRIES}`);
                    setTimeout(() => {
                        connectSSE();
                    }, 3000);
                } else {
                    console.error("최대 재연결 시도 횟수 초과");
                }
            };
        } catch (error) {
            console.error("SSE 연결 실패:", error);
        }
    }

    function closeConnection() {
        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }
    }

    onMount(() => {
        connectSSE();
    });

    onDestroy(() => {
        closeConnection();
    });
</script>

<div class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
    <div class="rounded-lg bg-white p-6 shadow-md">
        <div class="flex items-center">
            <div class="rounded-full bg-blue-100 p-3 text-blue-500">
                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
                    />
                </svg>
            </div>
            <div class="ml-4">
                <h2 class="text-sm text-gray-600">Queued</h2>
                <p class="text-2xl font-semibold text-gray-800">{stats.queuedCount}</p>
            </div>
        </div>
    </div>

    <div class="rounded-lg bg-white p-6 shadow-md">
        <div class="flex items-center">
            <div class="rounded-full bg-yellow-100 p-3 text-yellow-500">
                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                </svg>
            </div>
            <div class="ml-4">
                <h2 class="text-sm text-gray-600">Retry</h2>
                <p class="text-2xl font-semibold text-gray-800">{stats.retryCount}</p>
            </div>
        </div>
    </div>

    <div class="rounded-lg bg-white p-6 shadow-md">
        <div class="flex items-center">
            <div class="rounded-full bg-green-100 p-3 text-green-500">
                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M5 13l4 4L19 7"
                    />
                </svg>
            </div>
            <div class="ml-4">
                <h2 class="text-sm text-gray-600">Sent</h2>
                <p class="text-2xl font-semibold text-gray-800">{stats.sentCount}</p>
            </div>
        </div>
    </div>

    <div class="rounded-lg bg-white p-6 shadow-md">
        <div class="flex items-center">
            <div class="rounded-full bg-red-100 p-3 text-red-500">
                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                </svg>
            </div>
            <div class="ml-4">
                <h2 class="text-sm text-gray-600">Failed</h2>
                <p class="text-2xl font-semibold text-gray-800">{stats.failedCount}</p>
            </div>
        </div>
    </div>
</div>