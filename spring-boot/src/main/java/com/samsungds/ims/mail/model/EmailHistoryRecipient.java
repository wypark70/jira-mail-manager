package com.samsungds.ims.mail.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "email_history_recipient")
public class EmailHistoryRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;  // 수신자 이메일

    @Enumerated(EnumType.STRING)
    private EmailQueueRecipient.RecipientType type;  // 수신자 유형 (TO, CC, BCC)

    @ManyToOne
    @JoinColumn(name = "email_history_id")
    private EmailHistory emailHistory;
}