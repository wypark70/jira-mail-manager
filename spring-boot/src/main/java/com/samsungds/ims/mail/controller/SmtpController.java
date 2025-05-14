package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.service.SmtpServerService;
import com.samsungds.ims.mail.dto.LogMessage;
import com.samsungds.ims.mail.service.EmailQueueProcessLogService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/smtp")
public class SmtpController {

    private final SmtpServerService smtpServerService;
    private final EmailQueueProcessLogService emailQueueProcessLogService;

    public SmtpController(SmtpServerService smtpServerService, EmailQueueProcessLogService emailQueueProcessLogService) {
        this.smtpServerService = smtpServerService;
        this.emailQueueProcessLogService = emailQueueProcessLogService;
    }

    @GetMapping(path = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<LogMessage>> streamLogs() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.CONNECTION, "keep-alive")
                .body(emailQueueProcessLogService.getLogStream());
    }

    @PostMapping("/start")
    public String startServer() {
        smtpServerService.startSmtpServer();
        return "SMTP 서버가 시작되었습니다.";
    }

    @PostMapping("/stop")
    public String stopServer() {
        smtpServerService.stopSmtpServer();
        return "SMTP 서버가 중지되었습니다.";
    }

    @GetMapping("/status")
    public Map<String, Object> getServerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", smtpServerService.isRunning());
        status.put("status", smtpServerService.getServerStatus());
        return status;
    }
}
