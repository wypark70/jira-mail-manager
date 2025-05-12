package com.samsungds.ims.mail.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.repository.EmailQueueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEmailProcessor {
    
    private final EmailQueueRepository emailQueueRepository;

    @Async("emailExecutor")
    @Transactional
    public CompletableFuture<Void> processEmail(EmailQueue email) {
        try {
            log.info("이메일 ID {} 비동기 발송 처리 시작...", email.getId());
            emailQueueRepository.lockEmailForProcessing(
                email.getId(),
                Thread.currentThread().getName(),
                LocalDateTime.now()
            );
            
            // 이메일 발송 처리
            int processingTime = (int)(Math.random() * 10 + 1) * 1000;
            
            // 처리 시간이 5초를 초과하면 실패로 처리
            if (processingTime > 5000) {
                throw new Exception(String.format("처리 시간(%d초)이 5초 제한을 초과했습니다", processingTime/1000));
            }

            // 실제 처리 시간만큼 대기
            Thread.sleep(processingTime);
            log.info("이메일 ID {} 발송 소요시간: {}초", email.getId(), processingTime/1000);

            // 발송 성공 시 상태 업데이트
            email.markAsSent();
            emailQueueRepository.save(email);
            log.info("이메일 ID {} 발송 완료", email.getId());
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("이메일 ID {} 발송 실패: {}", email.getId(), e.getMessage());
            handleEmailError(email, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    private void handleEmailError(EmailQueue email, Exception e) {
        try {
            email.incrementRetry();
            if (email.getRetryCount() >= email.getMaxRetries()) {
                email.markAsFailed(e.getMessage());
            } else {
                email.setErrorMessage(e.getMessage());
            }
            emailQueueRepository.save(email);
        } catch (Exception ex) {
            log.error("오류 처리 중 추가 오류 발생", ex);
        }
    }
}