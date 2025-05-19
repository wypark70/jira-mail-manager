package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.dto.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class EmailQueueBatchLogService {
    private static final Duration TIMEOUT = Duration.ofMinutes(30);
    private final AtomicInteger activeSubscribers = new AtomicInteger(0);
    private final Sinks.Many<LogMessage> logSink = Sinks.many().multicast().onBackpressureBuffer(10000, false); // 버퍼 크기 10000으로 설정
    private final Flux<LogMessage> sharedLogStream;

    public EmailQueueBatchLogService() {
        this.sharedLogStream = logSink.asFlux().doOnSubscribe(s -> {
            int count = activeSubscribers.incrementAndGet();
            log.info("새로운 구독자 연결 - 세션 ID: {}, 현재 구독자 수: {}", s.hashCode(), count);
        }).doOnCancel(() -> {
            int count = activeSubscribers.decrementAndGet();
            log.info("구독자 연결 종료 - 현재 구독자 수: {}, 시간: {}", count, LocalDateTime.now());
        }).timeout(TIMEOUT).onErrorResume(e -> {
            log.error("스트림 에러 발생: {}, 현재 구독자 수: {}", e.getMessage(), activeSubscribers.get());
            return Flux.empty();
        }).retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(5))).publish().refCount(1, Duration.ofSeconds(10));
    }

    public void addLog(LogMessage logMessage) {
        try {
            if (logSink.currentSubscriberCount() == 0) {
                log.debug("현재 활성 구독자가 없습니다.");
                return;
            }
            Sinks.EmitResult result = logSink.tryEmitNext(logMessage);
            if (result.isFailure()) {
                log.error("로그 추가 실패: {}", result.name());
            }
        } catch (Exception e) {
            log.error("로그 처리 중 오류 발생", e);
        }
    }

    public Flux<LogMessage> getLogStream() {
        return sharedLogStream.doOnError(e -> log.error("스트림 에러 발생", e)).retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(30)).doBeforeRetry(signal -> log.info("스트림 재연결 시도 #{}", signal.totalRetries())));
    }

    public boolean hasActiveSubscribers() {
        return activeSubscribers.get() > 0;
    }

    public int getSubscriberCount() {
        return activeSubscribers.get();
    }

}