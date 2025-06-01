package com.samsungds.ims.mail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samsungds.ims.mail.model.EmailQueueAttachment;

public interface EmailQueueAttachmentRepository extends JpaRepository<EmailQueueAttachment, Long> {
    List<EmailQueueAttachment> findByEmailQueueId(Long emailQueueId);
}
