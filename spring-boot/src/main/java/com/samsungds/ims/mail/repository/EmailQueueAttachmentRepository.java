package com.samsungds.ims.mail.repository;

import java.util.Collection;
import java.util.List;

import com.samsungds.ims.mail.model.EmailQueueRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import com.samsungds.ims.mail.model.EmailQueueAttachment;

public interface EmailQueueAttachmentRepository extends JpaRepository<EmailQueueAttachment, Long> {
    List<EmailQueueAttachment> findByEmailQueueId(Long emailQueueId);
    List<EmailQueueAttachment> findAllByEmailQueueIdIn(Collection<Long> emailQueueIds);
    void deleteAllByEmailQueueIdIn(Collection<Long> emailQueueIds);
}
