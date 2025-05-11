package com.samsungds.ims.mail.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "email_history_content")
public class EmailHistoryContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String body;  // 이메일 본문

    @OneToOne
    @JoinColumn(name = "email_history_id")
    private EmailHistory emailHistory;
}