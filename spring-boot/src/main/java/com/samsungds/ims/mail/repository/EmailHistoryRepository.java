package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailHistory;
import com.samsungds.ims.mail.model.EmailQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface EmailHistoryRepository extends JpaRepository<EmailHistory, Long> {
    @Query("SELECT e FROM EmailHistory e WHERE " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:start IS NULL OR e.sentAt >= :start) AND " +
            "(:end IS NULL OR e.sentAt <= :end) AND " +
            "(:subject IS NULL OR LOWER(e.subject) LIKE LOWER(CONCAT('%', :subject, '%')))")
    Page<EmailHistory> findByStatusAndSubjectAndSentAtBetween(
            @Param("status") EmailQueue.EmailStatus status,
            @Param("subject") String subject,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}