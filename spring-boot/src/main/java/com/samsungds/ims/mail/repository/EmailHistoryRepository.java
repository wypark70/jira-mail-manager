package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailHistory;
import com.samsungds.ims.mail.model.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailHistoryRepository extends JpaRepository<EmailHistory, Long> {
    Optional<EmailHistory> findByOriginalEmailId(Long originalEmailId);
    List<EmailHistory> findBySentAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<EmailHistory> findByStatus(EmailQueue.EmailStatus status);
    List<EmailHistory> findByProcessorId(String processorId);
}