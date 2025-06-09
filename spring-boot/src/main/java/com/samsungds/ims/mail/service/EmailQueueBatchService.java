package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.component.SendMailBatchProperties;
import com.samsungds.ims.mail.dto.ProcessorStatus;
import com.samsungds.ims.mail.model.EmailQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class EmailQueueBatchService implements SmartLifecycle, SchedulingConfigurer {

    private final EmailQueueBatchAsyncService emailQueueBatchAsyncService;
    private final EmailQueueService emailQueueService;
    private final String processorId = generateProcessorId();
    private final SendMailBatchProperties sendMailBatchProperties;
    private volatile boolean running = false;

    // processorId 생성 방법
    private String generateProcessorId() {
        try {
            return String.join(
                    "-",
                    InetAddress.getLocalHost().getHostName(),
                    String.valueOf(ProcessHandle.current().pid()),
                    UUID.randomUUID().toString().substring(0, 8)
            );
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

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
        return SmartLifecycle.super.isAutoStartup();
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

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                this::processEmailQueue,
                triggerContext -> {
                    CronTrigger cronTrigger = new CronTrigger(sendMailBatchProperties.getScheduleCron());
                    return cronTrigger.nextExecution(triggerContext);
                }
        );
    }

    /**
     * 대기 중인 이메일 처리 (20초마다 실행)
     */
    public void processEmailQueue() {
        if (!running) {
            log.debug("이메일 큐 프로세서가 실행 중이 아닙니다.");
            return;
        }

        log.info("이메일 큐 처리 시작, 프로세서 ID: {}", processorId);
        processEmailQueueInternal();
    }

    private void processEmailQueueInternal() {

        try {
            // 타임아웃된 이메일 잠금 해제 (장애 복구)
            emailQueueService.unlockTimedOutEmails();

            // 예약된 이메일 처리
            emailQueueService.processScheduledEmails();

            // 재시도 대상 이메일 처리
            emailQueueService.processRetryEmails();

            // 처리할 이메일 목록 조회 및 비동기 처리
            processQueuedEmails();

            log.info("이메일 큐 처리 완료");
        } catch (Exception e) {
            log.error("이메일 큐 처리 중 오류 발생", e);
        }
    }

    /**
     * 대기 중인 이메일을 일괄 처리하고 결과 수집
     */
    private void processQueuedEmails() {
        List<EmailQueue> allProcessedEmails = new ArrayList<>();
        int totalEmailsProcessed = 0;
        int batchSize = sendMailBatchProperties.getBatchSize();
        int concurrentBatchSize = sendMailBatchProperties.getConcurrentBatchSize();

        for (int batch = 0; batch < batchSize && totalEmailsProcessed < batchSize; batch += concurrentBatchSize) {
            List<EmailQueue> emailsToProcess = emailQueueService.fetchEmailsForProcessing(concurrentBatchSize);

            if (emailsToProcess.isEmpty()) {
                log.info("더 이상 처리할 이메일이 없습니다");
                break;
            }

            log.info("{}개의 이메일을 비동기 처리합니다", emailsToProcess.size());

            // 비동기 처리 요청 및 결과 수집
            List<EmailQueue> processedEmails = processEmailsBatch(emailsToProcess);

            // 처리된 이메일 결과 합치기
            allProcessedEmails.addAll(processedEmails);
            totalEmailsProcessed += processedEmails.size();

            // 배치 간 짧은 대기 시간 추가 (시스템 부하 감소)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // 모든 처리 결과 분석 및 요약
        if (!allProcessedEmails.isEmpty()) {
            analyzeEmailProcessingResults(allProcessedEmails);
        }
    }

    /**
     * 이메일 처리 결과 저장
     */
    private void saveEmailProcessingResults(List<EmailQueue> processedEmails) {
        emailQueueService.saveProcessedEmails(processedEmails);
    }

    /**
     * 이메일 처리 결과 분석
     */
    private void analyzeEmailProcessingResults(List<EmailQueue> processedEmails) {
        // 상태별 분류
        Map<EmailQueue.EmailStatus, List<EmailQueue>> emailsByStatus = processedEmails.stream()
                .collect(Collectors.groupingBy(EmailQueue::getStatus));

        // 상태별 통계
        StringBuilder summary = new StringBuilder("이메일 처리 결과 요약:\n");

        int totalEmails = processedEmails.size();
        summary.append(String.format("- 총 처리 이메일: %d개\n", totalEmails));

        // 성공 이메일
        List<EmailQueue> sentEmails = emailsByStatus.getOrDefault(EmailQueue.EmailStatus.SENT, Collections.emptyList());
        if (!sentEmails.isEmpty()) {
            summary.append(String.format("- 성공: %d개 (%.1f%%)\n",
                    sentEmails.size(),
                    (float) sentEmails.size() / totalEmails * 100));
        }

        // 실패 이메일
        List<EmailQueue> failedEmails = emailsByStatus.getOrDefault(EmailQueue.EmailStatus.FAILED, Collections.emptyList());
        if (!failedEmails.isEmpty()) {
            summary.append(String.format("- 실패: %d개 (%.1f%%)\n",
                    failedEmails.size(),
                    (float) failedEmails.size() / totalEmails * 100));

            // 상위 3개 오류 메시지 출력
            Map<String, Long> errorMessageCounts = failedEmails.stream()
                    .collect(Collectors.groupingBy(
                            email -> Objects.toString(email.getErrorMessage(), "알 수 없는 오류"),
                            Collectors.counting()
                    ));

            summary.append("- 주요 오류 유형:\n");
            errorMessageCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .forEach(entry ->
                            summary.append(String.format("  * %s (%d건)\n", entry.getKey(), entry.getValue()))
                    );
        }

        // 재시도 이메일
        List<EmailQueue> retryEmails = emailsByStatus.getOrDefault(EmailQueue.EmailStatus.RETRY, Collections.emptyList());
        if (!retryEmails.isEmpty()) {
            summary.append(String.format("- 재시도 예정: %d개 (%.1f%%)\n",
                    retryEmails.size(),
                    (float) retryEmails.size() / totalEmails * 100));
        }

        log.info(summary.toString());
    }

    /**
     * 이메일 배치 처리
     * 비동기 처리와 결과 수집
     *
     * @return 처리된 이메일 목록
     */
    private List<EmailQueue> processEmailsBatch(List<EmailQueue> emails) {
        if (emails.isEmpty()) {
            return new ArrayList<>();
        }

        List<CompletableFuture<EmailQueue>> futures = new ArrayList<>();

        for (EmailQueue email : emails) {
            // 이메일 처리 전에 먼저 잠금 획득 시도
            EmailQueue lockedEmail = emailQueueService.tryLockEmailInNewTransaction(email, processorId);

            if (lockedEmail != null) {
                CompletableFuture<EmailQueue> future = emailQueueBatchAsyncService.processEmail(lockedEmail);
                futures.add(future);
            }
        }

        if (futures.isEmpty()) {
            return new ArrayList<>();
        }

        // 모든 비동기 처리 완료 대기 및 결과 수집
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 처리 결과 수집
        List<EmailQueue> processedEmails = futures.stream()
                .map(future -> {
                    try {
                        return future.get(); // 각 CompletableFuture에서 결과 가져오기
                    } catch (Exception e) {
                        log.error("비동기 처리 결과 가져오기 실패", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 처리 결과 요약 로깅
        int successCount = 0;
        int failCount = 0;
        int retryCount = 0;

        saveEmailProcessingResults(processedEmails);

        for (EmailQueue email : processedEmails) {
            if (email.getStatus() == EmailQueue.EmailStatus.SENT) {
                successCount++;
            } else if (email.getStatus() == EmailQueue.EmailStatus.FAILED) {
                failCount++;
            } else if (email.getStatus() == EmailQueue.EmailStatus.RETRY) {
                retryCount++;
            }
        }

        log.info("이메일 배치 처리 완료: 총 {}개 (성공: {}, 실패: {}, 재시도: {})",
                processedEmails.size(), successCount, failCount, retryCount);

        return processedEmails;
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
}