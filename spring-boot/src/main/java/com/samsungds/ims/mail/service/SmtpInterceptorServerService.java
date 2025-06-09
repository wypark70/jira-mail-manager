package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.component.SmtpInterceptorProperties;
import com.samsungds.ims.mail.component.SmtpMessageHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.subethamail.smtp.server.SMTPServer;

@Service
@Slf4j
public class SmtpInterceptorServerService {
    private final SmtpMessageHandlerFactory messageHandlerFactory;
    private final SmtpInterceptorProperties smtpInterceptorProperties;
    private SMTPServer smtpServer;
    private boolean running = false;

    public SmtpInterceptorServerService(SmtpMessageHandlerFactory messageHandlerFactory, SmtpInterceptorProperties smtpInterceptorProperties) {
        this.messageHandlerFactory = messageHandlerFactory;
        this.smtpInterceptorProperties = smtpInterceptorProperties;
    }

    public synchronized void startSmtpServer() {
        if (running) {
            log.info("SMTP 서버가 이미 실행 중입니다.");
            return;
        }

        try {
            int port = smtpInterceptorProperties.getPort();
            smtpServer = new SMTPServer(messageHandlerFactory);
            smtpServer.setPort(port);
            smtpServer.start();
            running = true;
            log.info("SMTP 서버가 시작되었습니다. (포트: {})", port);
        } catch (Exception e) {
            log.error("SMTP 서버 시작 중 오류 발생", e);
            if (smtpServer != null) {
                try {
                    smtpServer.stop();
                } catch (Exception ex) {
                    log.error("SMTP 서버 정리 중 오류 발생", ex);
                }
            }
            running = false;
        }

    }

    public synchronized void stopSmtpServer() {
        if (!running) {
            log.info("SMTP 서버가 이미 중지되었습니다.");
            return;
        }

        try {
            if (smtpServer != null) {
                smtpServer.stop();
                smtpServer = null;
            }
        } catch (Exception e) {
            log.error("SMTP 서버 종료 중 오류 발생", e);
        } finally {
            running = false;
            log.info("SMTP 서버가 종료되었습니다.");
        }

    }

    public boolean isRunning() {
        return smtpServer != null && smtpServer.isRunning();
    }
}