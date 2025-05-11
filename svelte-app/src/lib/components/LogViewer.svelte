<script lang="ts">
	import { onMount, onDestroy } from 'svelte';

	interface LogMessage {
		timestamp: string;
		level: 'INFO' | 'WARN' | 'ERROR';
		message: string;
	}

	// 상태 관리
	let logs = $state<LogMessage[]>([]);
	let eventSource: EventSource;
	let reconnectAttempts = 0;
	let logContainer: HTMLDivElement;
	let isConnecting = $state(false);

	const MAX_RECONNECT_ATTEMPTS = 5;
	let maxLogLines = $state(1000); // 최대 로그 라인 수
	let autoScroll = $state(true); // 자동 스크롤 활성화 여부

	function connectEventSource() {
		if (isConnecting) return;
		
		// 기존 연결 정리
		if (eventSource) {
			eventSource.close();
		}

		try {
			isConnecting = true;
			eventSource = new EventSource('http://localhost:8080/api/smtp/logs', {
				withCredentials: true
			});

			eventSource.onopen = () => {
				isConnecting = false;
				reconnectAttempts = 0;
			};

			eventSource.onmessage = (event) => {
				const logMessage = JSON.parse(event.data);
				logs = [...logs, logMessage].slice(-maxLogLines);

				if (autoScroll && logContainer) {
					queueMicrotask(() => {
						logContainer.scrollTop = logContainer.scrollHeight;
					});
				}
			};

			eventSource.onerror = (error) => {
				console.error('SSE 에러:', error);
				cleanupEventSource();

				if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
					reconnectAttempts++;
					console.log(`재연결 시도 ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS}`);
					setTimeout(() => {
						isConnecting = false;
						connectEventSource();
					}, 3000);
				} else {
					console.error('최대 재연결 시도 횟수 초과');
					isConnecting = false;
				}
			};
		} catch (error) {
			console.error('EventSource 생성 중 오류:', error);
			isConnecting = false;
		}
	}

	function cleanupEventSource() {
		if (eventSource) {
			eventSource.close();
		}
		isConnecting = false;
	}

	// 스크롤 이벤트 핸들러
	function handleScroll() {
		if (!logContainer) return;

		const { scrollTop, scrollHeight, clientHeight } = logContainer;
		const isAtBottom = scrollHeight - scrollTop - clientHeight < 50;
		autoScroll = isAtBottom;
	}

	onMount(() => {
		connectEventSource();
		return cleanupEventSource;
	});

	onDestroy(cleanupEventSource);

	// 파생된 상태로 로그 레벨별 스타일 클래스 정의
	const getLogLevelClass = $derived((level: string) => {
		switch (level) {
			case 'INFO':
				return 'text-green-400';
			case 'WARN':
				return 'text-yellow-400';
			case 'ERROR':
				return 'text-red-400';
			default:
				return 'text-white';
		}
	});
</script>

<div class="flex flex-col gap-2">
    <div class="flex items-center justify-end gap-4 text-sm">
        <label class="flex items-center gap-2 text-gray-600 dark:text-gray-300">
            <input
                type="number"
                bind:value={maxLogLines}
                min="100"
                max="10000"
                step="100"
                class="w-20 rounded bg-gray-100 px-2 py-1 text-gray-900 
                       dark:bg-gray-700 dark:text-white"
            />
            <span>최대 라인 수</span>
        </label>
        <label class="flex items-center gap-2 text-gray-600 dark:text-gray-300">
            <input 
                type="checkbox" 
                bind:checked={autoScroll} 
                class="rounded bg-gray-100 dark:bg-gray-700" 
            />
            <span>자동 스크롤</span>
        </label>
    </div>

    <div
        bind:this={logContainer}
        onscroll={handleScroll}
        class="h-96 overflow-y-auto rounded-lg bg-gray-50 p-4 
               dark:bg-gray-900"
    >
        <div class="space-y-2">
			{#each logs as log}
				<div class="font-mono text-sm">
					<span class="text-gray-400">{new Date(log.timestamp).toLocaleString()}</span>
					<span class="ml-2 {getLogLevelClass(log.level)}">
						[{log.level}]
					</span>
					<span class="ml-2 text-white">{log.message}</span>
				</div>
			{/each}
		</div>
    </div>
</div>
