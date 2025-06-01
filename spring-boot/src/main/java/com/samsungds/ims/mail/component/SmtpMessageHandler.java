package com.samsungds.ims.mail.component;

import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.model.EmailQueueAttachment;
import com.samsungds.ims.mail.model.EmailQueueContent;
import com.samsungds.ims.mail.model.EmailQueueRecipient;
import com.samsungds.ims.mail.repository.EmailQueueAttachmentRepository;
import com.samsungds.ims.mail.repository.EmailQueueContentRepository;
import com.samsungds.ims.mail.repository.EmailQueueRecipientRepository;
import com.samsungds.ims.mail.repository.EmailQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.subethamail.smtp.MessageHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final EmailQueueAttachmentRepository emailAttachmentRepository;
    private final AllowDomainFilter allowDomainFilter;


    @Value("${mail.attachment.storage.path:./attachments}") // 첨부파일 저장 경로 설정
    private String attachmentStoragePath;

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
            String emailBody = parseEmailBodyContent(message);

            // 동일한 발신자와 제목의 메일큐 검색
            EmailQueue emailQueue = emailQueueRepository.findBySenderAndSubject(from, subject)
                .orElseGet(() -> {
                    // 새로운 메일큐 생성
                    EmailQueue newQueue = new EmailQueue();
                    newQueue.setSender(this.from); // Use instance field 'from'
                    newQueue.setSubject(subject);
                    newQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
                    EmailQueue savedQueue = emailQueueRepository.save(newQueue);

                    // 본문 저장
                    EmailQueueContent content = new EmailQueueContent();
                    content.setBody(emailBody);
                    content.setEmailQueue(savedQueue);
                    EmailQueueContentRepository.save(content);

                    return savedQueue;
                });

            // 첨부파일 저장 로직 추가
            saveAttachments(message, emailQueue);

            // 수신자가 존재하지 않는 경우에만 추가
            if (!emailQueueRecipientRepository.existsByEmailAndEmailQueue(to, emailQueue)) {
                EmailQueueRecipient recipient = new EmailQueueRecipient(); // Renamed to avoid conflict
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

    private String parseEmailBodyContent(MimeMessage message) throws MessagingException, IOException {
        String plainText = findPlainTextInMultipartRecursive(message);
        if (plainText != null) {
            return plainText;
        }

        String htmlText = findHtmlTextInMultipartRecursive(message);
        if (htmlText != null) {
            // HTML을 일반 텍스트로 변환하려면 Jsoup과 같은 라이브러리를 사용할 수 있습니다.
            // 예: return Jsoup.parse(htmlText).text();
            // 현재는 원시 HTML을 반환합니다.
            return htmlText;
        }

        // 메시지가 멀티파트가 아니고 text/plain 또는 text/html도 아닌 경우에 대한 폴백 처리
        Object content = message.getContent();
        if (content instanceof String) {
            log.info("이메일 본문이 단순 문자열입니다. Content-Type: {}", message.getContentType());
            return (String) content;
        }

        log.warn("이메일에서 텍스트 본문을 추출할 수 없습니다. Content-Type: {}", message.getContentType());
        return "";
    }

    private String findPlainTextInMultipartRecursive(Part p) throws MessagingException, IOException {
        String disposition = p.getDisposition();
        // 첨부 파일이거나 인라인 요소이지만 텍스트 또는 멀티파트 자체가 아닌 경우 건너뜁니다.
        if (disposition != null && (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE))) {
            if (!p.isMimeType("text/*") && !p.isMimeType("multipart/*")) {
                return null;
            }
        }

        if (p.isMimeType("text/plain")) {
            return (String) p.getContent();
        }

        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String found = findPlainTextInMultipartRecursive(mp.getBodyPart(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private String findHtmlTextInMultipartRecursive(Part p) throws MessagingException, IOException {
        String disposition = p.getDisposition();
        if (disposition != null && (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE))) {
            if (!p.isMimeType("text/*") && !p.isMimeType("multipart/*")) {
                return null;
            }
        }

        if (p.isMimeType("text/html")) {
            return (String) p.getContent();
        }

        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String found = findHtmlTextInMultipartRecursive(mp.getBodyPart(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void saveAttachments(Part part, EmailQueue emailQueue) throws MessagingException, IOException {
        String disposition = part.getDisposition();
        String contentType = part.getContentType();

        // 멀티파트인 경우 재귀적으로 처리
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                saveAttachments(multipart.getBodyPart(i), emailQueue);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(disposition) ||
                    (Part.INLINE.equalsIgnoreCase(disposition) && part.getFileName() != null && !part.getFileName().trim().isEmpty())) {
            // 첨부파일이거나 파일 이름이 있는 인라인 요소인 경우
            String originalFileName = part.getFileName();
            if (originalFileName == null || originalFileName.trim().isEmpty()) {
                log.warn("첨부파일 이름이 없습니다. Content-Type: {}", contentType);
                return;
            }

            // 파일명 정제 (보안 및 경로 문제 방지)
            String sanitizedFileName = sanitizeFileName(originalFileName);

            // 저장 경로 생성 (emailQueue ID별로 하위 디렉토리 사용)
            Path directoryPath = Paths.get(attachmentStoragePath, String.valueOf(emailQueue.getId()));
            Files.createDirectories(directoryPath); // 디렉토리가 없으면 생성

            Path filePath = directoryPath.resolve(sanitizedFileName);

            try (InputStream inputStream = part.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

                EmailQueueAttachment attachment = new EmailQueueAttachment();
                attachment.setEmailQueue(emailQueue);
                attachment.setFileName(originalFileName); // 원본 파일명 저장
                attachment.setContentType(contentType);
                attachment.setFilePath(filePath.toAbsolutePath().toString());
                emailAttachmentRepository.save(attachment);

                log.info("첨부파일 저장 완료: {} (경로: {})", originalFileName, filePath.toAbsolutePath());
            } catch (Exception e) {
                log.error("첨부파일 '{}' 저장 중 오류 발생 (EmailQueue ID: {})", originalFileName, emailQueue.getId(), e);
                // 필요에 따라 예외를 다시 던지거나 처리 방식을 결정
            }
        }
        // 그 외 텍스트/HTML 파트 등은 parseEmailBodyContent에서 처리됨
    }

    private String sanitizeFileName(String fileName) {
        // 기본적인 파일명 정제: 경로 문자 제거 및 길이 제한
        // 보다 강력한 정제를 위해서는 라이브러리 사용 또는 정규식 확장 고려
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9.\\-_ㄱ-ㅎㅏ-ㅣ가-힣]", "_");
        // 너무 긴 파일명 자르기 (예: 200자)
        return sanitized.length() > 200 ? sanitized.substring(0, 200) : sanitized;
    }
}
