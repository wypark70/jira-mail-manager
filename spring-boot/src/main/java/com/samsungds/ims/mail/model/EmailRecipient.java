package com.samsungds.ims.mail.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "email_recipients")
public class EmailRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;      // 수신자 이메일

    @Enumerated(EnumType.STRING)
    private RecipientType type; // TO, CC, BCC

    // 이메일 큐와의 관계
    @ManyToOne
    @JoinColumn(name = "email_queue_id")
    private EmailQueue emailQueue;

    public enum RecipientType {
        TO,
        CC,
        BCC
    }
}