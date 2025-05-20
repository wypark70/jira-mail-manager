package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.dto.EmailQueueStats;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class EmailQueueStreamService {
    private final EmailQueueService emailQueueService;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Set<String> activeEmitterIds = ConcurrentHashMap.newKeySet();


    public EmailQueueStreamService(EmailQueueService emailQueueService) {
        this.emailQueueService = emailQueueService;
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(1);
        this.taskScheduler.setThreadNamePrefix("email-stats-scheduler-");
        this.taskScheduler.initialize();
    }

    @PreDestroy
    public void destroy() {
        taskScheduler.shutdown();
    }

    public SseEmitter streamStats() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String emitterId = UUID.randomUUID().toString();
        activeEmitterIds.add(emitterId);

        emitter.onCompletion(() -> activeEmitterIds.remove(emitterId));
        emitter.onTimeout(() -> activeEmitterIds.remove(emitterId));

        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                EmailQueueStats stats = emailQueueService.getQueueStats();
                emitter.send(stats);
            } catch (Exception e) {
                log.info("통계 전송 중 오류 발생: {}", e.getMessage());
                emitter.completeWithError(e);
                activeEmitterIds.remove(emitterId);
            }
        }, Duration.ofSeconds(5));

        emitter.onCompletion(() -> {
            future.cancel(true);
            log.debug("통계 스트림 연결 종료: {}", emitterId);
        });

        return emitter;
    }

    public int getActiveEmitterCount() {
        return activeEmitterIds.size();
    }
}