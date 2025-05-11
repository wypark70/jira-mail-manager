package com.samsungds.ims.mail.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "email_contents")
public class EmailContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10000)
    private String body;        // 본문

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 이메일 큐와의 관계
    @OneToOne
    @JoinColumn(name = "email_queue_id")
    private EmailQueue emailQueue;
}