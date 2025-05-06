package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.service.SmtpServerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/smtp")
public class SmtpController {

    private final SmtpServerService smtpServerService;

    public SmtpController(SmtpServerService smtpServerService) {
        this.smtpServerService = smtpServerService;
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
