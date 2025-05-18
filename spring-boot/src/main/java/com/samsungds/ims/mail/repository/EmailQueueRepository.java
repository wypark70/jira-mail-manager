package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {

    List<EmailQueue> findByStatus(EmailQueue.EmailStatus status);

    List<EmailQueue> findByStatusOrderByPriorityAscCreatedAtAsc(EmailQueue.EmailStatus status);

    @Query("SELECT e FROM EmailQueue e WHERE e.status = 'SCHEDULED' AND e.scheduledAt <= :now AND e.locked = false")
    List<EmailQueue> findScheduledEmailsDueBefore(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM EmailQueue e WHERE e.status = 'RETRY' AND e.lastRetryAt <= :retryAfter AND e.locked = false")
    List<EmailQueue> findEmailsForRetry(@Param("retryAfter") LocalDateTime retryAfter);

    @Modifying
    @Query("UPDATE EmailQueue e SET e.locked = true, e.lockedAt = :now, e.status = 'PROCESSING', e.processorId = :processorId WHERE e.id = :id AND e.locked = false")
    int lockEmailForProcessing(@Param("id") Long id, @Param("processorId") String processorId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE EmailQueue e SET e.locked = false, e.status = 'QUEUED' WHERE e.status = 'PROCESSING' AND e.lockedAt <= :timeout")
    int unlockTimedOutEmails(@Param("timeout") LocalDateTime timeout);

    Optional<EmailQueue> findBySenderAndSubject(String sender, String subject);

    @Query("SELECT e.status, COUNT(e) FROM EmailQueue e GROUP BY e.status")
    List<Object[]> countAllByStatus();

    @Query("SELECT e FROM EmailQueue e WHERE " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:start IS NULL OR e.createdAt >= :start) AND " +
           "(:end IS NULL OR e.createdAt <= :end) AND " +
           "(:subject IS NULL OR LOWER(e.subject) LIKE LOWER(CONCAT('%', :subject, '%')))")
    Page<EmailQueue> findByStatusAndSubject(
        @Param("status") EmailQueue.EmailStatus status,
        @Param("subject") String subject,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );
}