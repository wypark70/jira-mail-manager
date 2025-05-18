export type AlertType = 'success' | 'error' | 'warning' | 'info';

export interface AlertState {
    show: boolean;
    message: string;
    type: AlertType;
    id: string;
}

export interface AlertOptions {
    duration?: number;
    type?: AlertType;
}