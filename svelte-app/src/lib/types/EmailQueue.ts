export interface EmailQueue {
    id: number;
    sender: string;
    recipient: string;
    subject: string;
    body: string;
    attachments?: string;
    status: 'QUEUED' | 'PROCESSING' | 'SENT' | 'FAILED' | 'RETRY' | 'CANCELLED' | 'SCHEDULED';
    priority: number;
    retryCount: number;
    maxRetries: number;
    createdAt: string;
    updatedAt: string;
    scheduledAt?: string;
    sentAt?: string;
    lastRetryAt?: string;
    errorMessage?: string;
    processorId?: string;
    uniqueId?: string;
    cc?: string;
    bcc?: string;
    tags?: string;
    source?: string;
    locked: boolean;
    lockedAt?: string;
}