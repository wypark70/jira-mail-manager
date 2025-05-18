import { writable } from 'svelte/store';
import type { AlertState } from './types';

function createAlertStore() {
    const { subscribe, set, update } = writable<AlertState[]>([]);

    return {
        subscribe,
        add: (alert: AlertState) => update(alerts => [...alerts, alert]),
        remove: (id: string) => update(alerts => alerts.filter(a => a.id !== id)),
        clear: () => set([])
    };
}

export const alerts = createAlertStore();