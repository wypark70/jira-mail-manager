package com.samsungds.ims.mail.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.samsungds.ims.mail.component.SendMailBatchProperties;
import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.model.EmailQueueRecipient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailQueueBatchAsyncService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SendMailBatchProperties sendMailBatchProperties;

    public EmailQueueBatchAsyncService(SendMailBatchProperties sendMailBatchProperties) {
        this.sendMailBatchProperties = sendMailBatchProperties;
    }

    /**
     * 이메일 비동기 처리
     * 처리 결과로 최종 상태의 EmailQueue 객체 반환
     */
    @Async("emailExecutor")
    public CompletableFuture<EmailQueue> processEmail(EmailQueue email) {
        try {
            log.info("이메일 ID {} 비동기 발송 처리 시작...", email.getId());
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.addMixIn(EmailQueue.class, EmailQueueMixin.class);
            log.info("이메일 발송 시작: {}", objectMapper.writeValueAsString(email));
            if (isAllowedSendKnoxEmail(email)) {
                sendKnoxMail(email);
            } else {
                simulateEmailSending(email);
            }
            email.setLocked(false);
            email.setLockedAt(null);
            email.markAsSent();
            return CompletableFuture.completedFuture(email);
        } catch (Exception e) {
            log.error("이메일 ID {} 발송 실패: {}", email.getId(), e.getMessage());
            email.setLocked(false);
            email.setLockedAt(null);
            email.incrementRetry(e.getMessage());
            return CompletableFuture.completedFuture(email);
        }
    }

    private boolean isAllowedSendKnoxEmail(EmailQueue email) {
        String sender = email.getSender();
        boolean isAllowedSender = "wypark70@samsung.com".equals(sender) || "ehkim71@partner.samsung.com".equals(sender) || "eypark70@samsung.com".equals(sender);
        if (!isAllowedSender) return false;

        List<String> recipients = email.getRecipients().stream().map(EmailQueueRecipient::getEmail).toList();
        return recipients.stream().allMatch(recipient -> "wypark70@samsung.com".equals(recipient) || "ehkim71@partner.samsung.com".equals(recipient) || "eypark70@samsung.com".equals(recipient));
    }

    // processorId 생성 방법
    private String generateProcessorId() {
        try {
            return String.join(
                    "-",
                    InetAddress.getLocalHost().getHostName(),
                    String.valueOf(ProcessHandle.current().pid()),
                    UUID.randomUUID().toString().substring(0, 8)
            );
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    private void sendKnoxMail(EmailQueue email) throws RuntimeException {
        log.info("email.id: {}", email.getId());
        log.info("knoxApiUrl: {}", sendMailBatchProperties.getKnoxApiUrl());
        log.info("knoxApiKey: {}", sendMailBatchProperties.getKnoxApiKey());
        log.info("knoxApiSecret: {}", sendMailBatchProperties.getKnoxApiSecret());
        throw new RuntimeException("Knox API KEY ERROR");
    }

    /**
     * 테스트/개발 환경용 모의 이메일 발송
     */
    private void simulateEmailSending(EmailQueue email) throws Exception {
        // 이메일 전송 시뮬레이션 (지연 발생)
        int processingTime = (int) (Math.random() * 10 + 1) * 300; // 0.3~3초 랜덤 처리 시간

        // 50% 확률로 실패 시뮬레이션
        boolean simulateFailure = Math.random() < 0.5;

        if (simulateFailure) {
            // 테스트용 실패 시뮬레이션
            Thread.sleep(processingTime);
            throw new Exception("50% 확률로 실패 시뮬레이션");
        }

        // 발송 지연 시뮬레이션
        Thread.sleep(processingTime);

        // 발송 성공 로그
        log.info("이메일 ID {} 모의 발송 완료 (처리 시간: {}ms)", email.getId(), processingTime);

        // 상세 발송 정보 로그 (디버그용)
        if (log.isDebugEnabled()) {
            log.debug(
                    "발송된 이메일 상세 정보 - ID: {}, 제목: {}, 발신자: {}",
                    email.getId(),
                    email.getSubject(),
                    email.getSender()
            );
        }
    }

    @JsonIgnoreProperties({"content", "recipients", "attachments"})
    static class EmailQueueMixin {
    }
}
