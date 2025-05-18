import { alerts } from './AlertStore';
import type { AlertOptions, AlertState, AlertType } from './types';

class AlertService {
    private static instance: AlertService;
    private readonly defaultDuration = 3000;

    private constructor() {}

    static getInstance(): AlertService {
        if (!AlertService.instance) {
            AlertService.instance = new AlertService();
        }
        return AlertService.instance;
    }

    show(message: string, options: AlertOptions = {}): void {
        const id = crypto.randomUUID();
        const alert: AlertState = {
            show: true,
            message,
            type: options.type || 'info',
            id
        };

        alerts.add(alert);

        setTimeout(() => {
            this.dismiss(id);
        }, options.duration || this.defaultDuration);
    }

    success(message: string, options: AlertOptions = {}): void {
        this.show(message, { ...options, type: 'success' });
    }

    error(message: string, options: AlertOptions = {}): void {
        this.show(message, { ...options, type: 'error' });
    }

    warning(message: string, options: AlertOptions = {}): void {
        this.show(message, { ...options, type: 'warning' });
    }

    info(message: string, options: AlertOptions = {}): void {
        this.show(message, { ...options, type: 'info' });
    }

    dismiss(id: string): void {
        alerts.remove(id);
    }

    clear(): void {
        alerts.clear();
    }
}

export const alertService = AlertService.getInstance();