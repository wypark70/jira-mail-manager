package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.handler.SmtpMessageHandlerFactory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.subethamail.smtp.server.SMTPServer;

@Service
@Slf4j
public class SmtpServerService {

    private static final int SMTP_PORT = 25;
    private final SmtpMessageHandlerFactory messageHandlerFactory;
    private SMTPServer smtpServer;
    private boolean running = false;

    public SmtpServerService(SmtpMessageHandlerFactory messageHandlerFactory) {
        this.messageHandlerFactory = messageHandlerFactory;
    }

    public synchronized void startSmtpServer() {
        if (running) {
            log.info("SMTP 서버가 이미 실행 중입니다.");
            return;
        }

        try {
            smtpServer = new SMTPServer(messageHandlerFactory);
            smtpServer.setPort(SMTP_PORT);
            smtpServer.start();
            running = true;
            log.info("SMTP 서버가 시작되었습니다. (포트: {})", SMTP_PORT);
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

    public String getServerStatus() {
        return isRunning() ? "실행 중" : "중지됨";
    }
}