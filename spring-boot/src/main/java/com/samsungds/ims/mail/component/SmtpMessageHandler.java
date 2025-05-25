package com.samsungds.ims.mail.component;

import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.model.EmailQueueContent;
import com.samsungds.ims.mail.model.EmailQueueRecipient;
import com.samsungds.ims.mail.repository.EmailQueueContentRepository;
import com.samsungds.ims.mail.repository.EmailQueueRecipientRepository;
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
    private final EmailQueueRecipientRepository emailQueueRecipientRepository;
    private final EmailQueueContentRepository EmailQueueContentRepository;
    private final AllowDomainFilter allowDomainFilter;

    private String from;
    private String to;

    @Override
    public void from(String from) {
        this.from = from;
        log.info("보낸 사람 (From): {}", from);
    }

    @Override
    public void recipient(String recipient) {
        this.to = recipient;
        log.info("받는 사람 (To): {}", recipient);
    }

    @Override
    public void data(InputStream data) {
        if (!allowDomainFilter.isAllowedEmailDomain(from)) {
            log.warn("허용되지 않는 도메인입니다. (From: {})", from);
            return;
        }
        if (!allowDomainFilter.isAllowedEmailDomain(to)) {
            log.warn("허용되지 않는 도메인입니다. (To: {})", to);
            return;
        }
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage message = new MimeMessage(session, data);

            String subject = message.getSubject();
            String body = message.getContent().toString();

            // 동일한 발신자와 제목의 메일큐 검색
            EmailQueue emailQueue = emailQueueRepository.findBySenderAndSubject(from, subject)
                .orElseGet(() -> {
                    // 새로운 메일큐 생성
                    EmailQueue newQueue = new EmailQueue();
                    newQueue.setSender(from);
                    newQueue.setSubject(subject);
                    newQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
                    EmailQueue savedQueue = emailQueueRepository.save(newQueue);

                    // 본문 저장
                    EmailQueueContent content = new EmailQueueContent();
                    content.setBody(body);
                    content.setEmailQueue(savedQueue);
                    EmailQueueContentRepository.save(content);

                    return savedQueue;
                });

            // 수신자가 존재하지 않는 경우에만 추가
            if (!emailQueueRecipientRepository.existsByEmailAndEmailQueue(to, emailQueue)) {
                EmailQueueRecipient recipient = new EmailQueueRecipient();
                recipient.setEmail(to);
                recipient.setType(EmailQueueRecipient.RecipientType.TO);
                recipient.setEmailQueue(emailQueue);
                emailQueueRecipientRepository.save(recipient);
                log.info("새로운 수신자 추가: {} (제목: {}, 발신자: {})", to, subject, from);
            } else {
                log.info("이미 존재하는 수신자: {} (제목: {}, 발신자: {})", to, subject, from);
            }

            log.info("이메일 처리 완료. 제목: {}, 발신자: {}", subject, from);

        } catch (Exception e) {
            log.error("이메일 처리 중 오류 발생", e);
            throw new RuntimeException("이메일 처리 실패", e);
        }
    }

    @Override
    public void done() {
        log.info("SMTP 요청 처리가 완료되었습니다.");
    }
}
