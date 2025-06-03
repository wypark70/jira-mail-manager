package com.samsungds.ims.mail.component;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
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
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.subethamail.smtp.MessageHandler;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
@RequiredArgsConstructor
@Slf4j
public class SmtpMessageHandler implements MessageHandler {

    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String MIME_TEXT_HTML = "text/html";
    private static final String MIME_MULTIPART_ANY = "multipart/*";
    private static final String DISPOSITION_ATTACHMENT = Part.ATTACHMENT;
    private static final String DISPOSITION_INLINE = Part.INLINE;
    private final EmailQueueRepository emailQueueRepository;
    private final EmailQueueRecipientRepository emailQueueRecipientRepository;
    private final EmailQueueContentRepository emailQueueContentRepository;
    private final EmailQueueAttachmentRepository emailAttachmentRepository;
    private final AllowDomainFilter allowDomainFilter;
    private final String attachmentStoragePath;

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
        if (!validateEmailDomains(this.from, this.to)) {
            return;
        }

        try {
            // 1. MIME 메시지 파싱
            MimeMessage message = parseMimeMessage(data);
            String subject = message.getSubject();

            // 2. HTML 및 텍스트 본문 추출 (HTML 우선)
            String htmlBody = findBodyTextRecursive(message, MIME_TEXT_HTML);
            String textBody = null;
            if (htmlBody == null) {
                textBody = findBodyTextRecursive(message, MIME_TEXT_PLAIN);
            }

            // 3. 실제 발신자 결정 (메일 헤더의 From 또는 본문 내 Jira 사용자 정보)
            String effectiveSender = this.from; // 기본값
            String bodyForSenderDetection = htmlBody != null ? htmlBody : textBody;
            if (bodyForSenderDetection != null) {
                String senderFromBody = getSenderFromBody(bodyForSenderDetection);
                if (senderFromBody != null) {
                    effectiveSender = senderFromBody;
                }
            }

            // 4. EmailQueue 엔티티 생성 또는 조회 (본문 내용 없이)
            EmailQueue emailQueue = findOrCreateEmailQueueStub(effectiveSender, subject);

            // 5. CID 이미지 처리 및 HTML 본문 업데이트
            String finalBodyToStore;
            Map<String, String> cidToUrlMap = Collections.emptyMap();

            if (htmlBody != null) {
                cidToUrlMap = saveCidImagesAndGetMap(message, emailQueue);
                finalBodyToStore = replaceCidSrcInHtml(htmlBody, cidToUrlMap);
            } else if (textBody != null) {
                finalBodyToStore = textBody;
            } else {
                finalBodyToStore = "";
                log.warn("이메일에서 본문(text/plain 또는 text/html)을 추출할 수 없습니다. 제목: {}", subject);
            }

            // 6. 최종적으로 결정된 본문을 EmailQueueContent에 저장
            saveEmailContent(emailQueue, finalBodyToStore);

            // 7. 수신자 정보 추가
            addRecipientIfNotExists(this.to, emailQueue);

            // 8. CID로 처리되지 않은 나머지 첨부파일 저장
            saveOtherAttachments(message, emailQueue, cidToUrlMap.keySet());

            log.info("이메일 처리 완료. 제목: {}, 발신자: {}", subject, effectiveSender);

        } catch (Exception e) {
            log.error("이메일 처리 중 오류 발생", e);
            throw new RuntimeException("이메일 처리 실패", e);
        }
    }

    @Override
    public void done() {
        log.info("SMTP 요청 처리가 완료되었습니다.");
    }

    private boolean validateEmailDomains(String fromAddress, String toAddress) {
        if (!allowDomainFilter.isAllowedEmailDomain(fromAddress)) {
            log.warn("허용되지 않는 발신자 이메일 도메인입니다. (From: {})", fromAddress);
            return false;
        }
        if (!allowDomainFilter.isAllowedEmailDomain(toAddress)) {
            log.warn("허용되지 않는 수신자 이메일 도메인입니다. (To: {})", toAddress);
            return false;
        }
        return true;
    }

    private MimeMessage parseMimeMessage(InputStream data) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        return new MimeMessage(session, data);
    }

    // EmailQueue 엔티티만 생성/조회 (Content 제외)
    private EmailQueue findOrCreateEmailQueueStub(String sender, String subject) {
        return emailQueueRepository.findBySenderAndSubject(sender, subject)
                .orElseGet(() -> {
                    EmailQueue newQueue = new EmailQueue();
                    newQueue.setSender(sender);
                    newQueue.setSubject(subject);
                    newQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
                    EmailQueue savedQueue = emailQueueRepository.save(newQueue);
                    log.info("새로운 메일 큐 생성됨 (ID: {}, 발신자: {}, 제목: {})", savedQueue.getId(), sender, subject);
                    return savedQueue;
                });
    }

    private void saveEmailContent(EmailQueue emailQueue, String body) {
        EmailQueueContent content = emailQueueContentRepository.findByEmailQueue(emailQueue)
                .orElseGet(EmailQueueContent::new);
        content.setEmailQueue(emailQueue);
        content.setBody(body);
        emailQueueContentRepository.save(content);
    }

    private void addRecipientIfNotExists(String recipientEmail, EmailQueue emailQueue) {
        if (!emailQueueRecipientRepository.existsByEmailAndEmailQueue(recipientEmail, emailQueue)) {
            EmailQueueRecipient recipient = new EmailQueueRecipient();
            recipient.setEmail(recipientEmail);
            recipient.setType(EmailQueueRecipient.RecipientType.TO);
            recipient.setEmailQueue(emailQueue);
            emailQueueRecipientRepository.save(recipient);
            log.info("새로운 수신자 추가: {} (큐 ID: {}, 제목: {})", recipientEmail, emailQueue.getId(), emailQueue.getSubject());
        } else {
            log.info("이미 존재하는 수신자: {} (큐 ID: {}, 제목: {})", recipientEmail, emailQueue.getId(), emailQueue.getSubject());
        }
    }

    private String findBodyTextRecursive(Part part, String targetMimeType) throws MessagingException, IOException {
        String disposition = part.getDisposition();
        // 명시적으로 첨부파일인 경우, 본문으로 간주하지 않음
        if (DISPOSITION_ATTACHMENT.equalsIgnoreCase(disposition)) {
            return null;
        }

        // 대상 MIME 타입과 일치하고, 명시적인 첨부파일이 아닌 경우 (위에서 이미 disposition 체크함)
        // 또한 인라인 이미지 등도 본문으로 간주하지 않도록 fileName 유무도 체크 (선택적)
        if (part.isMimeType(targetMimeType)) {
            return (String) part.getContent();
        }

        if (part.isMimeType(MIME_MULTIPART_ANY)) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bodyPart = mp.getBodyPart(i);
                String found = findBodyTextRecursive(bodyPart, targetMimeType);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private boolean isActualAttachment(Part part) throws MessagingException {
        String disposition = part.getDisposition();
        String fileName = part.getFileName();

        if (DISPOSITION_ATTACHMENT.equalsIgnoreCase(disposition)) {
            return true;
        }
        return DISPOSITION_INLINE.equalsIgnoreCase(disposition) && fileName != null && !fileName.trim().isEmpty();
    }

    private Map<String, String> saveCidImagesAndGetMap(Part messagePart, EmailQueue emailQueue) throws MessagingException, IOException {
        Map<String, String> cidToUrlMap = new HashMap<>();
        collectAndSaveCidImagesRecursive(messagePart, emailQueue, cidToUrlMap);
        return cidToUrlMap;
    }

    private void collectAndSaveCidImagesRecursive(Part part, EmailQueue emailQueue, Map<String, String> cidToUrlMap) throws MessagingException, IOException {
        if (part.isMimeType(MIME_MULTIPART_ANY)) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                collectAndSaveCidImagesRecursive(multipart.getBodyPart(i), emailQueue, cidToUrlMap);
            }
        } else {
            String[] cids = part.getHeader("Content-ID");
            if (cids != null && cids.length > 0) {
                String cidHeaderValue = cids[0];
                String actualCid = cidHeaderValue.trim();
                if (actualCid.startsWith("<") && actualCid.endsWith(">")) {
                    actualCid = actualCid.substring(1, actualCid.length() - 1);
                }

                if (cidToUrlMap.containsKey(actualCid)) { // 이미 처리된 CID는 건너뜀
                    return;
                }

                String mimeType = part.getContentType();
                // ContentType에서 charset과 같은 부가 정보를 제거하고 순수 MIME 타입만 추출 (예: "image/png; charset=UTF-8" -> "image/png")
                if (mimeType != null && mimeType.contains(";")) {
                    mimeType = mimeType.substring(0, mimeType.indexOf(";")).trim();
                }
                if (mimeType == null || !mimeType.startsWith("image/")) {
                    log.warn("CID '{}'는 이미지 타입이 아닙니다 ({}). 건너<0xEB><0><0x8A><0x88>니다.", actualCid, mimeType);
                    return;
                }

                try (InputStream inputStream = part.getInputStream()) {
                    byte[] imageBytes = inputStream.readAllBytes();
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    String dataUri = "data:" + mimeType + ";base64," + base64Image;

                    cidToUrlMap.put(actualCid, dataUri);
                    log.info("CID 이미지 Base64 인코딩 완료: CID={}, MIME_TYPE={}, (큐 ID: {})", actualCid, mimeType, emailQueue.getId());
                } catch (IOException e) {
                    log.error("CID 이미지 (CID: {}) Base64 인코딩 중 IO 오류 발생 (큐 ID: {})", actualCid, emailQueue.getId(), e);
                } catch (Exception e) {
                    log.error("CID 이미지 (CID: {}) Base64 인코딩 중 예기치 않은 오류 발생 (큐 ID: {})", actualCid, emailQueue.getId(), e);
                }
            }
        }
    }

    private String replaceCidSrcInHtml(String htmlBody, Map<String, String> cidToUrlMap) {
        if (cidToUrlMap.isEmpty()) {
            return htmlBody;
        }
        Document doc = Jsoup.parse(htmlBody);
        for (Element imgTag : doc.select("img[src^=cid:]")) {
            String cidSrc = imgTag.attr("src");
            String cid = cidSrc.substring("cid:".length());
            if (cidToUrlMap.containsKey(cid)) {
                imgTag.attr("src", cidToUrlMap.get(cid));
                log.debug("CID 링크 변경: {} -> {}", cidSrc, cidToUrlMap.get(cid));
            }
        }
        return doc.html();
    }

    private void saveOtherAttachments(Part part, EmailQueue emailQueue, Set<String> processedCids) throws MessagingException, IOException {
        // 멀티파트인 경우 재귀적으로 처리
        if (part.isMimeType(MIME_MULTIPART_ANY)) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                saveOtherAttachments(multipart.getBodyPart(i), emailQueue, processedCids); // 재귀 호출
            }
        } else {
            // 이미 CID로 처리된 이미지는 건너뛴다.
            String[] contentIdHeader = part.getHeader("Content-ID");
            if (contentIdHeader != null && contentIdHeader.length > 0) {
                String actualCid = contentIdHeader[0].trim();
                if (actualCid.startsWith("<") && actualCid.endsWith(">")) {
                    actualCid = actualCid.substring(1, actualCid.length() - 1);
                }
                if (processedCids.contains(actualCid)) {
                    log.debug("일반 첨부파일 저장 건너뛰기 (이미 CID로 처리됨): {}", actualCid);
                    return;
                }
            }

            // 실제 첨부파일인 경우에만 저장 (예: Content-Disposition: attachment)
            if (isActualAttachment(part)) {
            String originalFileName = part.getFileName();
            String contentType = part.getContentType();

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

                    log.info("일반 첨부파일 저장 완료: {} (큐 ID: {}, 경로: {})", originalFileName, emailQueue.getId(), filePath.toAbsolutePath());
            } catch (IOException e) {
                    log.error("일반 첨부파일 '{}' 저장 중 IO 오류 발생 (큐 ID: {})", originalFileName, emailQueue.getId(), e);
                throw e;
            } catch (Exception e) {
                    log.error("일반 첨부파일 '{}' 처리 중 예기치 않은 오류 발생 (큐 ID: {})", originalFileName, emailQueue.getId(), e);
                    throw new MessagingException("일반 첨부파일 처리 중 오류: " + originalFileName, e);
            }
        }
        }
    }

    private String sanitizeFileName(String fileName) {
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9.\\-_ㄱ-ㅎㅏ-ㅣ가-힣]", "_");
        return sanitized.length() > 200 ? sanitized.substring(0, 200) : sanitized;
    }

    private String getSenderFromBody(String body) {
        Document document = Jsoup.parse(body);
        Element element = document.selectFirst("a.user-hover");
        if (element != null) {
            String userName = element.attr("rel");
            return getSenderEmailByUserName(userName);
        }
        return null;
    }

    private String getSenderEmailByUserName(String userName) {
        try {
            HttpResponse<JsonNode> response = Unirest.get("http://localhost:2990/jira/rest/api/2/user/search")
                    .basicAuth("admin", "admin")
                    .queryString("username", userName)
                    .asJson();
            if (response.getStatus() != 200) return null;
            JSONArray users = response.getBody().getArray();
            return users.length() > 0 ? users.getJSONObject(0).getString("emailAddress") : null;
        } catch (UnirestException e) {
            return null;
        }
    }
}
