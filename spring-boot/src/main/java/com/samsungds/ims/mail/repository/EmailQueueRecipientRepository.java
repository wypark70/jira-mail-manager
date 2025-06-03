package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.model.EmailQueueRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EmailQueueRecipientRepository extends JpaRepository<EmailQueueRecipient, Long> {
    List<EmailQueueRecipient> findByEmailQueueId(Long emailQueueId);
    boolean existsByEmailAndEmailQueue(String email, EmailQueue emailQueue);
    List<EmailQueueRecipient> findAllByEmailQueueIdIn(Collection<Long> emailQueueIds);
    void deleteAllByEmailQueueIdIn(Collection<Long> emailQueueIds);
}