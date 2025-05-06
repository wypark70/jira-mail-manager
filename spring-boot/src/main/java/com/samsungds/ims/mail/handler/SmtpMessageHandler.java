package com.samsungds.ims.mail.handler;

import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.repository.EmailQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.subethamail.smtp.MessageHandler;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * SMTP 요청 처리 핸들러
 */
@RequiredArgsConstructor
@Slf4j
public class SmtpMessageHandler implements MessageHandler {

    private final EmailQueueRepository emailQueueRepository;
    private final String clientIp; // 클라이언트 IP

    private String from;
    private String to;

    @Override
    public void from(String from) {
        this.from = from;
        log.info("보낸 사람 (From): {} ({})", from, clientIp);
    }

    @Override
    public void recipient(String recipient) {
        this.to = recipient;
        log.info("받는 사람 (To): {} ({})", recipient, clientIp);
    }

    @Override
    public void data(InputStream data) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage message = new MimeMessage(session, data);

            String subject = message.getSubject();
            String body = message.getContent().toString();

            EmailQueue emailQueue = new EmailQueue();
            emailQueue.setSender(from);
            emailQueue.setRecipient(to);
            emailQueue.setSubject(subject);
            emailQueue.setBody(body);

            // 이메일 저장
            emailQueueRepository.save(emailQueue);

            log.info("이메일 저장 완료. 클라이언트 IP: {}", clientIp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void done() {
        log.info("SMTP 요청 처리가 완료되었습니다. 클라이언트 IP: {}", clientIp);
    }
}
