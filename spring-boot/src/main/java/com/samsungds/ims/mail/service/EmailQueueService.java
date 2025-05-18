package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.dto.EmailQueueStats;
import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.repository.EmailQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이메일 큐 처리에 필요한 트랜잭션 메서드들을 제공하는 서비스
 * 자기 호출(self-invocation) 문제를 해결하기 위해 @Transactional 메서드들을 별도 클래스로 분리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueService {

    private final EmailQueueRepository emailQueueRepository;

    @Value("${mail.batch.lock-timeout-minutes:10}")
    private int lockTimeoutMinutes;

    @Value("${mail.batch.retry-delay-minutes:15}")
    private int retryDelayMinutes;


    /**
     * 처리할 이메일 목록을 DB에서 조회
     */
    @Transactional(readOnly = true)
    public List<EmailQueue> fetchEmailsForProcessing(int concurrentBatchSize) {
        return emailQueueRepository.findByStatusOrderByPriorityAscCreatedAtAsc(EmailQueue.EmailStatus.QUEUED)
                .stream()
                .limit(concurrentBatchSize)
                .toList();
    }

    /**
     * 별도 트랜잭션에서 이메일 잠금 획득 시도
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmailQueue tryLockEmailInNewTransaction(EmailQueue email, String processorId) {
        try {
            int updated = emailQueueRepository.lockEmailForProcessing(
                    email.getId(),
                    processorId,
                    LocalDateTime.now()
            );

            if (updated > 0) {
                // 잠금 획득 성공 시 최신 상태로 조회
                return emailQueueRepository.findById(email.getId()).orElse(null);
            }

            return null;
        } catch (Exception e) {
            log.warn("이메일 ID {}에 대한 잠금 획득 실패: {}", email.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * 타임아웃된 이메일 잠금 해제
     */
    @Transactional
    public void unlockTimedOutEmails() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(lockTimeoutMinutes);
        int unlocked = emailQueueRepository.unlockTimedOutEmails(timeoutThreshold);
        log.info("타임아웃된 {} 개의 이메일 잠금이 해제되었습니다", unlocked);
    }

    /**
     * 예약된 이메일 처리
     */
    @Transactional
    public void processScheduledEmails() {
        LocalDateTime now = LocalDateTime.now();
        List<EmailQueue> scheduledEmails = emailQueueRepository.findScheduledEmailsDueBefore(now);
        log.info("{}개의 예약 이메일이 발송 예정입니다", scheduledEmails.size());

        if (!scheduledEmails.isEmpty()) {
            for (EmailQueue emailQueue : scheduledEmails) {
                emailQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
                emailQueueRepository.save(emailQueue);
            }
        }
    }

    /**
     * 처리된 이메일 상태 업데이트 - 선택적 필드만 업데이트
     */
    @Transactional
    public void saveProcessedEmails(List<EmailQueue> processedEmails) {
        if (processedEmails.isEmpty()) {
            return;
        }

        for (EmailQueue email : processedEmails) {
            EmailQueue existingEmail = emailQueueRepository.findById(email.getId())
                    .orElse(null);

            if (existingEmail == null) {
                log.warn("ID {}인 이메일을 찾을 수 없습니다. 상태 업데이트를 건너뜁니다.", email.getId());
                continue;
            }

            // 필요한 필드만 선택적으로 업데이트
            existingEmail.setStatus(email.getStatus());
            existingEmail.setRetryCount(email.getRetryCount());
            existingEmail.setLastRetryAt(email.getLastRetryAt());
            existingEmail.setLocked(email.isLocked());
            existingEmail.setProcessorId(email.getProcessorId());

            if (email.getStatus() == EmailQueue.EmailStatus.SENT) {
                existingEmail.setSentAt(email.getSentAt());
            } else if (email.getStatus() == EmailQueue.EmailStatus.FAILED) {
                existingEmail.setErrorMessage(email.getErrorMessage());
            }

            // 변경된 엔티티 저장
            emailQueueRepository.save(existingEmail);
        }

        log.info("{}개의 이메일 상태가 성공적으로 업데이트되었습니다.", processedEmails.size());
    }

    /**
     * 재시도 대상 이메일 처리
     */
    @Transactional
    public void processRetryEmails() {
        LocalDateTime retryThreshold = LocalDateTime.now().minusMinutes(retryDelayMinutes);
        List<EmailQueue> retryEmails = emailQueueRepository.findEmailsForRetry(retryThreshold);
        log.info("{}개의 이메일이 재시도 대상입니다", retryEmails.size());

        if (!retryEmails.isEmpty()) {
            for (EmailQueue emailQueue : retryEmails) {
                emailQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
                emailQueueRepository.save(emailQueue);
            }
        }
    }

    /**
     * 수동으로 PROCESSING 상태의 이메일을 SENT 상태로 변경
     * 문제 상황에서 관리자가 직접 호출할 수 있는 메서드
     */
    @Transactional
    public int forceMarkProcessingAsSent() {
        List<EmailQueue> processingEmails = emailQueueRepository.findByStatus(EmailQueue.EmailStatus.PROCESSING);
        int count = 0;

        for (EmailQueue email : processingEmails) {
            log.info("이메일 ID {}을(를) 강제로 SENT 상태로 변경합니다.", email.getId());
            email.markAsSent();
            emailQueueRepository.save(email);
            count++;
        }

        log.info("총 {}개의 이메일 상태를 PROCESSING에서 SENT로 강제 변경했습니다.", count);
        return count;
    }

    /**
     * 특정 이메일의 상태를 강제로 변경
     */
    @Transactional
    public boolean forceChangeEmailStatus(Long emailId, EmailQueue.EmailStatus newStatus) {
        EmailQueue email = emailQueueRepository.findById(emailId).orElse(null);

        if (email == null) {
            log.warn("ID {}의 이메일을 찾을 수 없습니다.", emailId);
            return false;
        }

        EmailQueue.EmailStatus oldStatus = email.getStatus();
        log.info("이메일 ID {}의 상태를 {}에서 {}로 강제 변경합니다.",
                emailId, oldStatus, newStatus);

        email.setStatus(newStatus);

        if (newStatus == EmailQueue.EmailStatus.SENT) {
            email.setSentAt(LocalDateTime.now());
            email.setLocked(false);
        } else if (newStatus == EmailQueue.EmailStatus.QUEUED) {
            email.setLocked(false);
        } else if (newStatus == EmailQueue.EmailStatus.FAILED) {
            email.setLocked(false);
            if (email.getErrorMessage() == null || email.getErrorMessage().isEmpty()) {
                email.setErrorMessage("관리자에 의해 실패 상태로 변경됨");
            }
        }

        emailQueueRepository.save(email);
        log.info("이메일 ID {}의 상태가 성공적으로 변경되었습니다.", emailId);
        return true;
    }

    /**
     * 이메일 큐 상태 조회
     * 단일 쿼리로 모든 상태의 개수를 가져와 성능을 최적화합니다.
     */
    @Transactional(readOnly = true)
    public EmailQueueStats getQueueStats() {
        EmailQueueStats stats = new EmailQueueStats();
        stats.setTotalCount(emailQueueRepository.count());

        // 기본값 0으로 설정
        stats.setQueuedCount(0);
        stats.setProcessingCount(0);
        stats.setSentCount(0);
        stats.setFailedCount(0);
        stats.setRetryCount(0);
        stats.setScheduledCount(0);

        // 모든 상태 개수를 한 번의 쿼리로 조회
        List<Object[]> statusCounts = emailQueueRepository.countAllByStatus();

        // 조회된 결과를 상태별로 매핑
        for (Object[] result : statusCounts) {
            EmailQueue.EmailStatus status = (EmailQueue.EmailStatus) result[0];
            long count = ((Number) result[1]).longValue();

            switch (status) {
                case QUEUED:
                    stats.setQueuedCount(count);
                    break;
                case PROCESSING:
                    stats.setProcessingCount(count);
                    break;
                case SENT:
                    stats.setSentCount(count);
                    break;
                case FAILED:
                    stats.setFailedCount(count);
                    break;
                case RETRY:
                    stats.setRetryCount(count);
                    break;
                case SCHEDULED:
                    stats.setScheduledCount(count);
                    break;
                case CANCELLED:
                    // 필요한 경우 추가
                    break;
                default:
                    // 추가 상태 처리
                    break;
            }
        }

        return stats;
    }

    @Transactional(readOnly = true)
    public Page<EmailQueue> findByStatusAndSubjectAndCreatedAtBetween(
            EmailQueue.EmailStatus status,
            String subject,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable) {
        return emailQueueRepository.findByStatusAndSubjectAndCreatedAtBetween(
                status,
                subject,
                start,
                end,
                pageable
        );
    }

}