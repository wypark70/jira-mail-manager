import { writable, get } from 'svelte/store';

interface SSEState {
    isConnected: boolean;
    error: string | null;
    eventSource: EventSource | null;
    retryCount: number;
}

function createSSEConnection() {
    const store = writable<SSEState>({
        isConnected: false,
        error: null,
        eventSource: null,
        retryCount: 0
    });

    let eventSource: EventSource | null = null;

    const connect = () => {
        if (eventSource) {
            disconnect();
        }

        try {
            // 단순히 EventSource만 생성
            eventSource = new EventSource('/api/smtp/logs');
        
            eventSource.onopen = () => {
                store.update(state => ({
                    ...state,
                    isConnected: true,
                    error: null,
                    retryCount: 0
                }));
            };

            eventSource.onmessage = (event) => {
            // event.lastEventId는 EventSource가 자동으로 관리
            // 서버가 이벤트와 함께 ID를 보내면 자동으로 저장됨
            };

            return eventSource;
        } catch (error) {
            store.update(state => ({
                ...state,
                isConnected: false,
                error: '연결을 생성할 수 없습니다',
            }));
            return null;
        }
    };

    const disconnect = () => {
        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }
        
        store.update(state => ({
            ...state,
            isConnected: false,
            error: null,
            eventSource: null,
            retryCount: 0
        }));
    };

    return {
        subscribe: store.subscribe,
        connect,
        disconnect
    };
}

export const sseConnection = createSSEConnection();