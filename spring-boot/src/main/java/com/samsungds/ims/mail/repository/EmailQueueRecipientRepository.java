package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.model.EmailQueueRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailQueueRecipientRepository extends JpaRepository<EmailQueueRecipient, Long> {
    List<EmailQueueRecipient> findByEmailQueueId(Long emailQueueId);
    
    /**
     * 이메일 주소와 이메일 큐로 수신자 존재 여부 확인
     */
    boolean existsByEmailAndEmailQueue(String email, EmailQueue emailQueue);
}