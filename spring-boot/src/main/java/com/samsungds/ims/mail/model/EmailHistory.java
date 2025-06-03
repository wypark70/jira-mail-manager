package com.samsungds.ims.mail.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"content", "recipients"})
@Table(name = "email_history")
public class EmailHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long originalEmailId;  // 원본 이메일 ID
    private String sender;         // 발신자
    private String subject;        // 제목

    // 본문 관계 설정
    @OneToOne(mappedBy = "emailHistory", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private EmailHistoryContent content;

    // 수신자 관계 설정
    @OneToMany(mappedBy = "emailHistory", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EmailHistoryRecipient> recipients = new ArrayList<>();

    @OneToMany(mappedBy = "emailHistory", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EmailHistoryAttachment> attachments = new ArrayList<>();

    private String processorId;    // 처리한 프로세서 ID
    private String errorMessage;   // 오류 메시지
    private int retryCount;        // 재시도 횟수
    
    @Enumerated(EnumType.STRING)
    private EmailQueue.EmailStatus status;  // 최종 상태

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime sentAt;     // 실제 발송 시간
}