package com.samsungds.ims.mail.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 이메일 큐 시스템의 현재 상태에 대한 통계 정보를 제공하는 클래스
 */
@Setter
@Getter
public class EmailQueueStats {
    private long queuedCount;        // 대기 중인 이메일 수
    private long processingCount;    // 처리 중인 이메일 수
    private long sentCount;          // 전송 완료된 이메일 수
    private long failedCount;        // 실패한 이메일 수
    private long retryCount;         // 재시도 예정인 이메일 수
    private long scheduledCount;     // 예약된 이메일 수
    private long totalCount;        // 전체 이메일 수
}