package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.model.EmailQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailQueueAsyncProcessorService {

    /**
     * 이메일 비동기 처리
     * 처리 결과로 최종 상태의 EmailQueue 객체 반환
     */
    @Async("emailExecutor")
    public CompletableFuture<EmailQueue> processEmail(EmailQueue email) {
        try {
            log.info("이메일 ID {} 비동기 발송 처리 시작...", email.getId());
            log.info("이메일 발송 시작 - ID: {}, 제목: {}", email.getId(), email.getSubject());
            simulateEmailSending(email);
            email.setProcessorId(null);
            email.setLocked(false);
            email.markAsSent();
            return CompletableFuture.completedFuture(email);
        } catch (Exception e) {
            log.error("이메일 ID {} 발송 실패: {}", email.getId(), e.getMessage());
            email.setProcessorId(null);
            email.setLocked(false);
            email.incrementRetry(e.getMessage());
            return CompletableFuture.completedFuture(email);
        }
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
}
