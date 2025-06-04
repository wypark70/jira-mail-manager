package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.dto.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class LogStreamService {
    private static final Duration TIMEOUT = Duration.ofMinutes(30);
    private static final Duration INITIAL_BACKOFF = Duration.ofSeconds(1);
    private static final Duration MAX_BACKOFF = Duration.ofSeconds(30);
    private static final int MAX_RETRIES = 10;
    
    private final AtomicInteger activeSubscribers = new AtomicInteger(0);
    private final AtomicBoolean isConnected = new AtomicBoolean(true);
    private final Sinks.Many<LogMessage> logSink = Sinks.many().multicast().onBackpressureBuffer(10000, false);
    private final Flux<LogMessage> sharedLogStream;

    public LogStreamService() {
        this.sharedLogStream = logSink.asFlux()
                .doOnSubscribe(s -> {
                    int count = activeSubscribers.incrementAndGet();
                    isConnected.set(true);
                    log.info("새로운 구독자 연결 - 세션 ID: {}, 현재 구독자 수: {}", s.hashCode(), count);
                })
                .doOnCancel(() -> {
                    int count = activeSubscribers.decrementAndGet();
                    isConnected.set(false);
                    log.info("구독자 연결 종료 - 현재 구독자 수: {}, 시간: {}", count, LocalDateTime.now());
                })
                .timeout(TIMEOUT)
                .onErrorContinue((error, obj) -> {
                    log.error("스트림 에러 발생: {}, 현재 구독자 수: {}", error.getMessage(), activeSubscribers.get());
                    isConnected.set(false);
                })
                .retryWhen(createRetrySpec())
                .publish()
                .refCount(1, Duration.ofMinutes(1));
    }

    private Retry createRetrySpec() {
        return Retry.backoff(MAX_RETRIES, INITIAL_BACKOFF)
                .maxBackoff(MAX_BACKOFF)
                .doBeforeRetry(signal -> {
                    log.info("연결 재시도 #{} - 백오프 시간: {}ms",
                            signal.totalRetries(),
                            INITIAL_BACKOFF.multipliedBy(1L << Math.min(signal.totalRetries(), 4)).toMillis());
                })
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    log.error("최대 재시도 횟수 도달 ({}회) - 연결 복구 실패", MAX_RETRIES);
                    isConnected.set(false);
                    return new RuntimeException("최대 재시도 횟수를 초과했습니다.");
                });
    }

    public Flux<LogMessage> getLogStream() {
        return sharedLogStream
                .doOnError(e -> {
                    log.error("스트림 에러 발생", e);
                    isConnected.set(false);
                })
                .retryWhen(createRetrySpec())
                .doOnNext(message -> {
                    if (!isConnected.get()) {
                        log.info("연결 복구 성공");
                        isConnected.set(true);
                    }
                });
    }

    public void addLog(LogMessage logMessage) {
        try {
            if (!hasActiveSubscribers()) {
                return;
            }
            Sinks.EmitResult result = logSink.tryEmitNext(logMessage);
            if (result.isFailure()) {
                log.error("로그 추가 실패: {}", result.name());
                isConnected.set(false);
            }
        } catch (Exception e) {
            log.error("로그 처리 중 오류 발생", e);
            isConnected.set(false);
        }
    }

    public boolean hasActiveSubscribers() {
        return activeSubscribers.get() > 0;
    }

    public int getSubscriberCount() {
        return activeSubscribers.get();
    }

    public boolean isConnected() {
        return isConnected.get();
    }
}