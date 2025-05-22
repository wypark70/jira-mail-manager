package com.samsungds.ims.mail.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "email_queue_recipients")
public class EmailQueueRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;      // 수신자 이메일

    @Enumerated(EnumType.STRING)
    private RecipientType type; // TO, CC, BCC

    // 이메일 큐와의 관계
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "email_queue_id")
    private EmailQueue emailQueue;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum RecipientType {
        TO,
        CC,
        BCC
    }
}