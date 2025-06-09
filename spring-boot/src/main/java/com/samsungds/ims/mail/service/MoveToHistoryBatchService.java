package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.component.MoveToHistoryBatchProperties;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class MoveToHistoryBatchService implements SmartLifecycle, SchedulingConfigurer {
    private final String processorId = generateProcessorId();
    private final EmailHistoryService emailHistoryService;
    private final MoveToHistoryBatchProperties moveToHistoryBatchProperties;
    private volatile boolean running = false;

    // processorId 생성
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
        log.info("히스토리 테일블로 이동처리 배치 시작 - 프로세서 ID: {}", processorId);
        running = true;
    }

    @Override
    public void stop() {
        log.info("히스토리 테일블로 이동처리 배치 종료 - 프로세서 ID: {}", processorId);
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
                this::processMoveToHistory,
                triggerContext -> {
                    CronTrigger cronTrigger = new CronTrigger(moveToHistoryBatchProperties.getScheduleCron());
                    return cronTrigger.nextExecution(triggerContext);
                }
        );
    }

    /**
     * 성공한 이메일 이력 테이블로 이동 (동적 cron에 의해 실행)
     */
    public void processMoveToHistory() {
        if (!running) {
            log.debug("히스토리 테일블로 이동처리 배치가 실행 중이 아닙니다.");
            return;
        }
        log.info("히스토리 테일블로 이동처리 배치 시작, 프로세서 ID: {}", processorId);
        processMoveToHistoryInternal();
    }

    private void processMoveToHistoryInternal() {
        try {
            int moveCnt = emailHistoryService.moveEmailsToHistoryByStatus(EmailQueue.EmailStatus.SENT);
            log.info("{}개의 이메일이 히스토리 테일블로 이동처리 되었습니다.", moveCnt);
        } catch (Exception e) {
            log.error("히스토리 테일블로 이동처리 배치 처리 중 오류 발생", e);
        }
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