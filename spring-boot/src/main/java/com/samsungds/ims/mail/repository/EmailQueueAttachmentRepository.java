package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailQueueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EmailQueueAttachmentRepository extends JpaRepository<EmailQueueAttachment, Long> {
    List<EmailQueueAttachment> findByEmailQueueId(Long emailQueueId);
    List<EmailQueueAttachment> findAllByEmailQueueIdIn(Collection<Long> emailQueueIds);
    void deleteAllByEmailQueueIdIn(Collection<Long> emailQueueIds);
}
