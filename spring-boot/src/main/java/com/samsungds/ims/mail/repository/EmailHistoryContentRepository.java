package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailHistoryContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailHistoryContentRepository extends JpaRepository<EmailHistoryContent, Long> {
    Optional<EmailHistoryContent> findByEmailHistoryId(Long emailHistoryId);
}