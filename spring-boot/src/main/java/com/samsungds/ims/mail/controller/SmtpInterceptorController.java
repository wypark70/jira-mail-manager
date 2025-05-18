package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.dto.LogMessage;
import com.samsungds.ims.mail.service.EmailQueueBatchLogService;
import com.samsungds.ims.mail.service.SmtpInterceptorServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/smtp-interceptor")
@Slf4j
public class SmtpInterceptorController {

    private final SmtpInterceptorServerService smtpInterceptorServerService;
    private final EmailQueueBatchLogService emailQueueBatchLogService;

    public SmtpInterceptorController(SmtpInterceptorServerService smtpInterceptorServerService, EmailQueueBatchLogService emailQueueBatchLogService) {
        this.smtpInterceptorServerService = smtpInterceptorServerService;
        this.emailQueueBatchLogService = emailQueueBatchLogService;
    }

    @GetMapping(path = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<LogMessage>> streamLogs(
            @RequestHeader HttpHeaders headers) {

        // 모든 헤더 정보 로깅
        log.info("===== 새로운 로그 스트림 연결 요청됨 =====");
        headers.forEach((name, values) -> {
            values.forEach(value -> {
                log.info("헤더 - {}: {}", name, value);
            });
        });
        log.info("=====================================");

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.CONNECTION, "keep-alive")
                .body(emailQueueBatchLogService.getLogStream()
                        .doOnComplete(() -> log.info("로그 스트림 완료"))
                        .doOnError(e -> log.error("로그 스트림 에러", e))
                );
    }

    @PostMapping("/start")
    public String startServer() {
        smtpInterceptorServerService.startSmtpServer();
        return "SMTP 서버가 시작되었습니다.";
    }

    @PostMapping("/stop")
    public String stopServer() {
        smtpInterceptorServerService.stopSmtpServer();
        return "SMTP 서버가 중지되었습니다.";
    }

    @GetMapping("/status")
    public Map<String, Object> getServerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", smtpInterceptorServerService.isRunning());
        status.put("status", smtpInterceptorServerService.getServerStatus());
        return status;
    }
}
