package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailHistoryRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailHistoryRecipientRepository extends JpaRepository<EmailHistoryRecipient, Long> {
    List<EmailHistoryRecipient> findByEmailHistoryId(Long emailHistoryId);
}