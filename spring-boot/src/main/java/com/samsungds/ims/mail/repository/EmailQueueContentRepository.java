package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailQueueContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailQueueContentRepository extends JpaRepository<EmailQueueContent, Long> {
    Optional<EmailQueueContent> findByEmailQueueId(Long emailQueueId);
}