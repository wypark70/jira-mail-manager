package com.samsungds.ims.mail.component;

import com.samsungds.ims.mail.service.SmtpInterceptorServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpServerLifecycleManager {

    private final SmtpInterceptorServerService smtpInterceptorServerService;

    /**
     * 애플리케이션이 시작되면 SMTP 서버 시작
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startSmtpServerOnStartup() {
        log.info("애플리케이션 시작 이벤트 감지, SMTP 서버 시작...");
        smtpInterceptorServerService.startSmtpServer();
    }

    /**
     * 애플리케이션이 종료되면 SMTP 서버 중지
     */
    @EventListener(ContextClosedEvent.class)
    public void stopSmtpServerOnShutdown() {
        log.info("애플리케이션 종료 이벤트 감지, SMTP 서버 중지...");
        smtpInterceptorServerService.stopSmtpServer();
    }
}