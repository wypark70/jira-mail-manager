import { writable } from 'svelte/store';

interface SSEState {
    eventSource: EventSource | null;
    isConnected: boolean;
    retryCount: number;
}

function createSSEStore() {
    const { subscribe, update } = writable<SSEState>({
        eventSource: null,
        isConnected: false,
        retryCount: 0
    });

    const MAX_RETRIES = 5;

    function connect() {
        update(state => {
            if (state.eventSource) {
                state.eventSource.close();
            }

            const es = new EventSource('/api/smtp/logs');
            
            es.onmessage = (event) => {
                // 로그 메시지를 처리하는 콜백을 등록할 수 있도록 이벤트 발생
                window.dispatchEvent(new CustomEvent('sse-log', { 
                    detail: JSON.parse(event.data) 
                }));
            };

            es.onerror = () => {
                if (state.retryCount < MAX_RETRIES) {
                    setTimeout(() => {
                        connect();
                    }, 3000);
                }
                return { 
                    ...state, 
                    isConnected: false,
                    retryCount: state.retryCount + 1 
                };
            };

            return { 
                ...state,
                eventSource: es,
                isConnected: true 
            };
        });
    }

    function disconnect() {
        update(state => {
            if (state.eventSource) {
                state.eventSource.close();
            }
            return {
                eventSource: null,
                isConnected: false,
                retryCount: 0
            };
        });
    }

    return {
        subscribe,
        connect,
        disconnect
    };
}

export const sseStore = createSSEStore();