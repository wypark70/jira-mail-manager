package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.dto.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class EmailQueueProcessLogService {
    private static final Duration TIMEOUT = Duration.ofMinutes(30);
    // 버퍼 크기를 명시적으로 설정하고 DROP_OLDEST 전략 사용
    private final Sinks.Many<LogMessage> logSink = Sinks.many().multicast()
            .onBackpressureBuffer(10000, false); // 버퍼 크기 10000으로 설정

    private final Flux<LogMessage> sharedLogStream;

    public EmailQueueProcessLogService() {
        this.sharedLogStream = logSink.asFlux()
                .timeout(TIMEOUT)
                .onErrorResume(e -> {
                    log.error("스트림 처리 중 오류", e);
                    return Flux.empty();
                })
                .doOnCancel(() -> log.info("클라이언트 연결 종료"))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(5)))
                .publish()
                .autoConnect();
    }

    public void addLog(LogMessage logMessage) {
        try {
            Sinks.EmitResult result = logSink.tryEmitNext(logMessage);
            if (result.isFailure()) {
                // 재시도 로직 추가
                for (int i = 0; i < 3; i++) {
                    if (result == Sinks.EmitResult.FAIL_OVERFLOW) {
                        log.debug("버퍼 오버플로우 발생, 재시도 중... (시도 {})", i + 1);
                        try {
                            Thread.sleep(100); // 잠시 대기 후 재시도
                            result = logSink.tryEmitNext(logMessage);
                            if (result.isSuccess()) {
                                return;
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                log.warn("로그 메시지 emit 실패: {}, 원인: {}", 
                    logMessage, result.name());
            }
        } catch (Exception e) {
            log.error("로그 처리 중 오류 발생", e);
        }
    }

    public Flux<LogMessage> getLogStream() {
        return sharedLogStream;
    }
}