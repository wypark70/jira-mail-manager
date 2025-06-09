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

    @OneToOne(mappedBy = "emailQueue", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private EmailQueueContent content;

    @OneToMany(mappedBy = "emailQueue", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EmailQueueRecipient> recipients;

    @OneToMany(mappedBy = "emailQueue", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EmailQueueAttachment> attachments;

    @Enumerated(EnumType.STRING)
    private EmailStatus status = EmailStatus.QUEUED;

    private int priority = 5;
    private int retryCount = 0;
    private int maxRetries = 3;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime scheduledAt;    // 예약 발송 시간
    private LocalDateTime sentAt;         // 실제 발송 시간
    private LocalDateTime lastRetryAt;    // 마지막 재시도 시간

    @Column(length = 2000)
    private String errorMessage;

    private String processorId;
    private String uniqueId;
    private String tags;
    private String source;
    private boolean locked = false;
    private LocalDateTime lockedAt;

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

    public void markAsSent() {
        this.status = EmailStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.locked = false;
    }

    public void markAsFailed(String errorMessage) {
        this.status = EmailStatus.FAILED;
        this.errorMessage = errorMessage;
        this.locked = false;
    }

    public enum EmailStatus {
        QUEUED,      // 대기 중
        PROCESSING,  // 처리 중
        SENT,        // 발송 완료
        FAILED,      // 실패
        RETRY,       // 재시도 예정
        CANCELLED,   // 취소됨
        SCHEDULED    // 예약됨
    }
}