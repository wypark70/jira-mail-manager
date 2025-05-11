package com.samsungds.ims.mail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.samsungds.ims.mail.model.EmailQueue;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {

    // 상태별 이메일 조회
    List<EmailQueue> findByStatus(EmailQueue.EmailStatus status);

    // 특정 상태의 이메일을 우선순위 순으로 조회 (낮은 숫자가 높은 우선순위)
    List<EmailQueue> findByStatusOrderByPriorityAscCreatedAtAsc(EmailQueue.EmailStatus status);

    // 발송 예정 시간이 현재보다 이전인 예약 이메일 조회
    @Query("SELECT e FROM EmailQueue e WHERE e.status = 'SCHEDULED' AND e.scheduledAt <= :now AND e.locked = false")
    List<EmailQueue> findScheduledEmailsDueBefore(@Param("now") LocalDateTime now);

    // 발송 실패 후 재시도 대상 조회
    @Query("SELECT e FROM EmailQueue e WHERE e.status = 'RETRY' AND e.lastRetryAt <= :retryAfter AND e.locked = false")
    List<EmailQueue> findEmailsForRetry(@Param("retryAfter") LocalDateTime retryAfter);

    // 처리 중인 이메일 중 타임아웃된 항목 조회 (락이 너무 오래 유지됨)
    @Query("SELECT e FROM EmailQueue e WHERE e.status = 'PROCESSING' AND e.lockedAt <= :timeout")
    List<EmailQueue> findTimedOutEmails(@Param("timeout") LocalDateTime timeout);

    // 배치 처리를 위한 이메일 조회 (잠금 획득)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmailQueue e WHERE e.status = :status AND e.locked = false ORDER BY e.priority ASC, e.createdAt ASC LIMIT 1")
    Optional<EmailQueue> findNextEmailForProcessingWithLock(@Param("status") EmailQueue.EmailStatus status);

    // 동시성을 고려한 이메일 큐 항목 잠금
    @Modifying
    @Query("UPDATE EmailQueue e SET e.locked = true, e.lockedAt = :now, e.status = 'PROCESSING', e.processorId = :processorId WHERE e.id = :id AND e.locked = false")
    int lockEmailForProcessing(@Param("id") Long id, @Param("processorId") String processorId, @Param("now") LocalDateTime now);

    // 특정 시간 후 처리되지 않은 이메일 자동 잠금 해제 (장애 복구용)
    @Modifying
    @Query("UPDATE EmailQueue e SET e.locked = false, e.status = 'QUEUED' WHERE e.status = 'PROCESSING' AND e.lockedAt <= :timeout")
    int unlockTimedOutEmails(@Param("timeout") LocalDateTime timeout);

    // 태그 기반 검색
    List<EmailQueue> findByTagsContaining(String tag);

    // 특정 날짜 범위 내 생성된 이메일 조회
    List<EmailQueue> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 특정 날짜 범위 내 발송된 이메일 조회
    List<EmailQueue> findBySentAtBetween(LocalDateTime start, LocalDateTime end);

    // 재시도 횟수가 특정 수 이상인 실패 이메일 조회
    List<EmailQueue> findByStatusAndRetryCountGreaterThanEqual(EmailQueue.EmailStatus status, int retryCount);

    // 특정 발신자의 이메일 조회
    List<EmailQueue> findBySender(String sender);

    // 제목 기반 검색
    List<EmailQueue> findBySubjectContaining(String keyword);

    // 상태별 이메일 통계
    @Query("SELECT e.status, COUNT(e) FROM EmailQueue e GROUP BY e.status")
    List<Object[]> countEmailsByStatus();

    // 특정 기간의 발송 성공률 계산
    @Query("SELECT COUNT(e) FROM EmailQueue e WHERE e.status = 'SENT' AND e.sentAt BETWEEN :start AND :end")
    long countSentEmailsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(e) FROM EmailQueue e WHERE e.createdAt BETWEEN :start AND :end")
    long countTotalEmailsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 제목으로 이메일 큐 검색
     */
    Optional<EmailQueue> findBySubject(String subject);

    Optional<EmailQueue> findBySenderAndSubject(String sender, String subject);
}