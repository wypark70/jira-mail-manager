package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.service.SmtpServerService;
import com.samsungds.ims.mail.dto.LogMessage;
import com.samsungds.ims.mail.service.LogService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/smtp")
public class SmtpController {

    private final SmtpServerService smtpServerService;
    private final LogService logService;

    public SmtpController(SmtpServerService smtpServerService, LogService logService) {
        this.smtpServerService = smtpServerService;
        this.logService = logService;
    }

    @GetMapping(path = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<LogMessage>> streamLogs() {
        return ResponseEntity.ok()
                .body(logService.getLogStream());
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
