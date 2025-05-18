// src/lib/types/index.ts
export interface LogMessage {
    timestamp: string;
    level: LogLevel;
    message: string;
}

export type LogLevel = 'INFO' | 'WARN' | 'ERROR' | 'DEBUG';