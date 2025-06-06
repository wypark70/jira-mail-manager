package com.samsungds.ims.mail.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"content", "recipients"})
@Table(name = "email_queue")
public class EmailQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;      // 이메일 발신자
    private String subject;     // 제목

    // 본문 관계 설정
    @OneToOne(mappedBy = "emailQueue", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private EmailQueueContent content;

    // 수신자 관계 설정
    @OneToMany(mappedBy = "emailQueue", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EmailQueueRecipient> recipients;

    @OneToMany(mappedBy = "emailQueue", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EmailQueueAttachment> attachments;

    // 이메일 상태 관리
    @Enumerated(EnumType.STRING)
    private EmailStatus status = EmailStatus.QUEUED;

    // 우선순위 (1: 높음, 5: 중간, 10: 낮음)
    private int priority = 5;

    // 재시도 관련 필드
    private int retryCount = 0;
    private int maxRetries = 3;

    // 처리 시간 관련 필드
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime scheduledAt;    // 예약 발송 시간
    private LocalDateTime sentAt;         // 실제 발송 시간
    private LocalDateTime lastRetryAt;    // 마지막 재시도 시간

    // 오류 정보
    @Column(length = 2000)
    private String errorMessage;

    // 발송자 식별 정보 (배치 처리 ID)
    private String processorId;

    // 중복 방지용 고유 식별자 (선택 사항)
    private String uniqueId;

    // 태그 또는 분류 (필터링, 검색용)
    private String tags;

    // 이메일의 출처 또는 템플릿 ID
    private String source;

    // 잠금 필드 (동시 처리 방지)
    private boolean locked = false;
    private LocalDateTime lockedAt;

    // 이메일 상태 열거형
    public enum EmailStatus {
        QUEUED,      // 대기 중
        PROCESSING,  // 처리 중
        SENT,        // 발송 완료
        FAILED,      // 실패
        RETRY,       // 재시도 예정
        CANCELLED,   // 취소됨
        SCHEDULED    // 예약됨
    }

    // 편의 메서드: 재시도 증가
    public void incrementRetry(String errorMessage) {
        this.lastRetryAt = LocalDateTime.now();

        if (this.retryCount >= this.maxRetries) {
            markAsFailed(errorMessage);
        } else {
            this.retryCount++;
            this.errorMessage = errorMessage;
            this.status = EmailStatus.RETRY;
        }
    }

    // 편의 메서드: 발송 완료 표시
    public void markAsSent() {
        this.status = EmailStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.locked = false;
    }

    // 편의 메서드: 실패 표시
    public void markAsFailed(String errorMessage) {
        this.status = EmailStatus.FAILED;
        this.errorMessage = errorMessage;
        this.locked = false;
    }
}