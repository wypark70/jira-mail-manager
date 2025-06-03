package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailHistoryAttachment;
import com.samsungds.ims.mail.model.EmailQueueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EmailHistoryAttachmentRepository extends JpaRepository<EmailHistoryAttachment, Long> {
    List<EmailHistoryAttachment> findByEmailHistoryId(Long emailQueueId);
    List<EmailHistoryAttachment> findAllByEmailHistoryIdIn(Collection<Long> emailQueueIds);
    void deleteAllByEmailHistoryIdIn(Collection<Long> emailQueueIds);
}
