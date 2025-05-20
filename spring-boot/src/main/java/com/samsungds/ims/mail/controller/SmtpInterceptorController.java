package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.dto.LogMessage;
import com.samsungds.ims.mail.service.RootLogStreamService;
import com.samsungds.ims.mail.service.SmtpInterceptorServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/smtp-interceptor")
@Slf4j
public class SmtpInterceptorController {

    private final SmtpInterceptorServerService smtpInterceptorServerService;
    private final RootLogStreamService rootLogStreamService;

    public SmtpInterceptorController(SmtpInterceptorServerService smtpInterceptorServerService, RootLogStreamService rootLogStreamService) {
        this.smtpInterceptorServerService = smtpInterceptorServerService;
        this.rootLogStreamService = rootLogStreamService;
    }

    @GetMapping(path = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LogMessage> streamLogs() {
        return rootLogStreamService.getLogStream();
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
        boolean isRunning = smtpInterceptorServerService.isRunning();
        status.put("running", isRunning);
        status.put("status", isRunning ? "RUNNING" : "STOPPED");
        return status;
    }
}
