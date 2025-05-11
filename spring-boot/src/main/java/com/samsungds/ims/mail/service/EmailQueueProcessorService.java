package com.samsungds.ims.mail.service;


import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.repository.EmailQueueRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.SmartLifecycle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.net.InetAddress;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueProcessorService implements SmartLifecycle {

    private final EmailQueueRepository emailQueueRepository;
    private final String processorId = generateProcessorId(); // 서비스 인스턴스마다 고유 ID
    private volatile boolean running = false;

    // processorId 생성 방법
    private String generateProcessorId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-"
                    + ProcessHandle.current().pid() + "-"
                    + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    @Value("${mail.batch.batch-size:10}")
    private int batchSize;
    @Value("${mail.batch.lock-timeout-minutes:10}")
    private int lockTimeoutMinutes;
    @Value("${mail.batch.retry-delay-minutes:15}")
    private int retryDelayMinutes;

    // SmartLifecycle 구현
    @Override
    public void start() {
        log.info("이메일 큐 프로세서 시작 - 프로세서 ID: {}", processorId);
        running = true;
    }

    @Override
    public void stop() {
        log.info("이메일 큐 프로세서 종료 - 프로세서 ID: {}", processorId);
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return 0;
    }

    /**
     * 대기 중인 이메일 처리 (1분마다 실행)
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processEmailQueue() {
        if (!running) {
            log.debug("이메일 큐 프로세서가 실행 중이 아닙니다.");
            return;
        }

        log.info("이메일 큐 처리 시작, 프로세서 ID: {}", processorId);

        try {
            // 타임아웃된 이메일 잠금 해제 (장애 복구)
            unlockTimedOutEmails();

            // 예약된 이메일 처리
            processScheduledEmails();

            // 재시도 대상 이메일 처리
            processRetryEmails();

            // 대기 중인 이메일 처리
            int processedCount = 0;
            for (int i = 0; i < batchSize; i++) {
                boolean processed = processNextEmail();
                if (!processed) break;
                processedCount++;
            }

            log.info("이메일 큐 처리 완료, 처리된 이메일 수: {}", processedCount);
        } catch (Exception e) {
            log.error("이메일 큐 처리 중 오류 발생", e);
        }
    }

    /**
     * 타임아웃된 이메일 잠금 해제
     */
    @Transactional
    public void unlockTimedOutEmails() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(lockTimeoutMinutes);
        int unlocked = emailQueueRepository.unlockTimedOutEmails(timeoutThreshold);

        if (unlocked > 0) {
            log.info("타임아웃된 {} 개의 이메일 잠금이 해제되었습니다", unlocked);
        }
    }

    /**
     * 예약된 이메일 처리
     */
    @Transactional
    public void processScheduledEmails() {
        LocalDateTime now = LocalDateTime.now();
        List<EmailQueue> scheduledEmails = emailQueueRepository.findScheduledEmailsDueBefore(now);

        if (!scheduledEmails.isEmpty()) {
            log.info("{}개의 예약 이메일이 발송 예정입니다", scheduledEmails.size());

            for (EmailQueue emailQueue : scheduledEmails) {
                emailQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
                emailQueueRepository.save(emailQueue);
            }
        }
    }

    /**
     * 재시도 대상 이메일 처리
     */
    @Transactional
    public void processRetryEmails() {
        LocalDateTime retryThreshold = LocalDateTime.now().minusMinutes(retryDelayMinutes);
        List<EmailQueue> retryEmails = emailQueueRepository.findEmailsForRetry(retryThreshold);

        if (!retryEmails.isEmpty()) {
            log.info("{}개의 이메일이 재시도 대상입니다", retryEmails.size());

            for (EmailQueue emailQueue : retryEmails) {
                emailQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
                emailQueueRepository.save(emailQueue);
            }
        }
    }

    /**
     * 다음 이메일 처리
     */
    @Transactional
    public boolean processNextEmail() {
        // 다음 처리할 이메일 조회
        EmailQueue email = emailQueueRepository.findNextEmailForProcessingWithLock(EmailQueue.EmailStatus.QUEUED)
                .orElse(null);

        if (email == null) {
            return false;
        }

        try {
            // 여기서 실제 이메일 발송 처리
            try {
                log.info("이메일 ID {} 발송 처리 중...", email.getId());
                emailQueueRepository.lockEmailForProcessing(
                        email.getId(),
                        processorId,  // 현재 프로세스의 ID
                        LocalDateTime.now()
                );
                sendEmail(email);

                // 발송 성공 시 상태 업데이트
                email.markAsSent();
                emailQueueRepository.save(email);
                log.info("이메일 ID {} 발송 완료", email.getId());
            } catch (Exception e) {
                log.error("이메일 ID {} 발송 실패: {}", email.getId(), e.getMessage());

                // 발송 실패 시 재시도 횟수 증가 또는 실패 상태로 변경
                email.incrementRetry();
                if (email.getRetryCount() >= email.getMaxRetries()) {
                    email.markAsFailed(e.getMessage());
                }
                emailQueueRepository.save(email);
            }

        } catch (Exception e) {
            log.error("이메일 처리 중 오류 발생: {} - 프로세서: {}", e.getMessage(), processorId, e);

            // 오류 발생 시 이메일 잠금 해제 시도
            try {
                EmailQueue errorEmail = emailQueueRepository.findById(email.getId()).orElse(null);
                if (errorEmail != null && errorEmail.isLocked() &&
                        errorEmail.getStatus() == EmailQueue.EmailStatus.PROCESSING) {
                    errorEmail.markAsFailed("처리 중 시스템 오류: " + e.getMessage());
                    emailQueueRepository.save(errorEmail);
                }
            } catch (Exception ex) {
                log.error("오류 복구 중 추가 오류 발생", ex);
            }
        }

        return true;
    }

    /**
     * 실제 이메일 발송 처리
     */
    private void sendEmail(EmailQueue emailQueue) throws Exception {
        // 실제 구현에서는 여기서 이메일 발송 로직 실행
        // 테스트를 위한 지연 추가 (실제 구현에서는 제거)
        Thread.sleep(1000);  // 이메일 발송에 1초 소요된다고 가정
        
        /*MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(emailQueue.getSender());
        helper.setTo(emailQueue.getRecipient());
        helper.setSubject(emailQueue.getSubject());
        helper.setText(emailQueue.getBody(), true); // HTML 지원
        
        // CC 및 BCC 설정
        if (emailQueue.getCc() != null && !emailQueue.getCc().isEmpty()) {
            helper.setCc(emailQueue.getCc().split(","));
        }
        
        if (emailQueue.getBcc() != null && !emailQueue.getBcc().isEmpty()) {
            helper.setBcc(emailQueue.getBcc().split(","));
        }
        
        // 첨부 파일 처리 (구현 필요)
        
        // 이메일 발송
        mailSender.send(message);*/
    }

    /**
     * 이메일 큐 상태 조회
     */
    public EmailQueueStats getQueueStats() {
        EmailQueueStats stats = new EmailQueueStats();

        stats.setQueuedCount(emailQueueRepository.findByStatus(EmailQueue.EmailStatus.QUEUED).size());
        stats.setProcessingCount(emailQueueRepository.findByStatus(EmailQueue.EmailStatus.PROCESSING).size());
        stats.setSentCount(emailQueueRepository.findByStatus(EmailQueue.EmailStatus.SENT).size());
        stats.setFailedCount(emailQueueRepository.findByStatus(EmailQueue.EmailStatus.FAILED).size());
        stats.setRetryCount(emailQueueRepository.findByStatus(EmailQueue.EmailStatus.RETRY).size());
        stats.setScheduledCount(emailQueueRepository.findByStatus(EmailQueue.EmailStatus.SCHEDULED).size());

        return stats;
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
     * 배치 프로세서의 현재 상태 조회
     */
    public ProcessorStatus getProcessorStatus() {
        return ProcessorStatus.builder()
                .processorId(processorId)
                .running(running)
                .startedAt(LocalDateTime.now())
                .build();
    }

    @Getter
    @Builder
    public static class ProcessorStatus {
        private final String processorId;
        private final boolean running;
        private final LocalDateTime startedAt;
    }

    // 이메일 큐 통계 클래스
    @Setter
    @Getter
    public static class EmailQueueStats {
        private int queuedCount;
        private int processingCount;
        private int sentCount;
        private int failedCount;
        private int retryCount;
        private int scheduledCount;
    }
}