package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.repository.EmailQueueRepository;
import com.samsungds.ims.mail.service.EmailQueueProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/email-queue")
@RequiredArgsConstructor
@Slf4j
public class EmailQueueController {

    private final EmailQueueRepository emailQueueRepository;
    private final EmailQueueProcessorService emailQueueProcessorService;

    /**
     * 이메일 큐에 새 이메일 추가
     */
    @PostMapping
    public ResponseEntity<EmailQueue> addEmail(@RequestBody EmailQueue emailQueue) {
        emailQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
        emailQueue.setCreatedAt(LocalDateTime.now());

        // 예약 시간이 설정된 경우
        if (emailQueue.getScheduledAt() != null && emailQueue.getScheduledAt().isAfter(LocalDateTime.now())) {
            emailQueue.setStatus(EmailQueue.EmailStatus.SCHEDULED);
        }

        EmailQueue savedEmail = emailQueueRepository.save(emailQueue);
        return ResponseEntity.ok(savedEmail);
    }

    /**
     * 예약 이메일 큐에 추가
     */
    @PostMapping("/schedule")
    public ResponseEntity<EmailQueue> scheduleEmail(
            @RequestBody EmailQueue email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt) {

        email.setStatus(EmailQueue.EmailStatus.SCHEDULED);
        email.setScheduledAt(scheduledAt);
        email.setCreatedAt(LocalDateTime.now());

        EmailQueue savedEmail = emailQueueRepository.save(email);
        return ResponseEntity.ok(savedEmail);
    }

    /**
     * 큐에서 이메일 취소
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> cancelEmail(@PathVariable Long id) {
        Optional<EmailQueue> emailOpt = emailQueueRepository.findById(id);

        if (emailOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmailQueue emailQueue = emailOpt.get();

        // 이미 발송된 이메일은 취소 불가
        if (emailQueue.getStatus() == EmailQueue.EmailStatus.SENT) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "이미 발송된 이메일은 취소할 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 처리 중인 이메일도 취소 불가
        if (emailQueue.getStatus() == EmailQueue.EmailStatus.PROCESSING) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "현재 처리 중인 이메일은 취소할 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        emailQueue.setStatus(EmailQueue.EmailStatus.CANCELLED);
        emailQueueRepository.save(emailQueue);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "이메일이 취소되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 큐 상태 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<EmailQueueProcessorService.EmailQueueStats> getQueueStats() {
        EmailQueueProcessorService.EmailQueueStats stats = emailQueueProcessorService.getQueueStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 특정 상태의 이메일 목록 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmailQueue>> getEmailsByStatus(@PathVariable String status) {
        try {
            EmailQueue.EmailStatus emailStatus = EmailQueue.EmailStatus.valueOf(status.toUpperCase());
            List<EmailQueue> emailQueueList = emailQueueRepository.findByStatus(emailStatus);
            return ResponseEntity.ok(emailQueueList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 실패한 이메일 재시도 큐에 추가
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<Map<String, Object>> retryFailedEmail(@PathVariable Long id) {
        Optional<EmailQueue> emailOpt = emailQueueRepository.findById(id);

        if (emailOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmailQueue emailQueue = emailOpt.get();

        // 실패한 이메일만 재시도 가능
        if (emailQueue.getStatus() != EmailQueue.EmailStatus.FAILED) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "실패한 이메일만 재시도할 수 있습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 재시도 설정
        emailQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
        emailQueue.setRetryCount(0); // 재시도 카운트 리셋
        emailQueue.setErrorMessage(null);
        emailQueue.setUpdatedAt(LocalDateTime.now());
        emailQueueRepository.save(emailQueue);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "이메일 재시도가 큐에 추가되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 처리 중인 이메일 모니터링
     */
    @GetMapping("/processing")
    public ResponseEntity<List<EmailQueue>> getProcessingEmails() {
        List<EmailQueue> emailQueueList = emailQueueRepository.findByStatus(EmailQueue.EmailStatus.PROCESSING);
        return ResponseEntity.ok(emailQueueList);
    }

    /**
     * 이메일 상세 정보 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmailQueue> getEmailDetails(@PathVariable Long id) {
        Optional<EmailQueue> emailQueue = emailQueueRepository.findById(id);
        return emailQueue.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 이메일 큐 수동 처리 (관리자용)
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processEmailQueue() {
        try {
            emailQueueProcessorService.processEmailQueue();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이메일 큐 처리가 시작되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("이메일 큐 수동 처리 중 오류 발생", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "처리 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 배치 프로세서 시작
     */
    @PostMapping("/processor/start")
    public ResponseEntity<Map<String, Object>> startProcessor() {
        Map<String, Object> response = new HashMap<>();
        
        if (emailQueueProcessorService.isRunning()) {
            response.put("success", false);
            response.put("message", "배치 프로세서가 이미 실행 중입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            emailQueueProcessorService.start();
            EmailQueueProcessorService.ProcessorStatus status = emailQueueProcessorService.getProcessorStatus();
            
            response.put("success", true);
            response.put("message", "배치 프로세서가 시작되었습니다.");
            response.put("processorId", status.getProcessorId());
            response.put("startedAt", status.getStartedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "배치 프로세서 시작 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 배치 프로세서 종료
     */
    @PostMapping("/processor/stop")
    public ResponseEntity<Map<String, Object>> stopProcessor() {
        Map<String, Object> response = new HashMap<>();
        
        if (!emailQueueProcessorService.isRunning()) {
            response.put("success", false);
            response.put("message", "배치 프로세서가 이미 중지된 상태입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            emailQueueProcessorService.stop();
            
            response.put("success", true);
            response.put("message", "배치 프로세서가 중지되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "배치 프로세서 중지 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 배치 프로세서 상태 조회
     */
    @GetMapping("/processor/status")
    public ResponseEntity<EmailQueueProcessorService.ProcessorStatus> getProcessorStatus() {
        EmailQueueProcessorService.ProcessorStatus status = emailQueueProcessorService.getProcessorStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * 특정 날짜 범위의 이메일 조회
     */
    @GetMapping("/range")
    public ResponseEntity<List<EmailQueue>> getEmailsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<EmailQueue> emails = emailQueueRepository.findByCreatedAtBetween(start, end);
        return ResponseEntity.ok(emails);
    }

    /**
     * 이메일 성공률 통계
     */
    @GetMapping("/success-rate")
    public ResponseEntity<Map<String, Object>> getEmailSuccessRateStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        long sentCount = emailQueueRepository.countSentEmailsBetween(start, end);
        long totalCount = emailQueueRepository.countTotalEmailsBetween(start, end);

        double successRate = totalCount > 0 ? (double) sentCount / totalCount * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("sentCount", sentCount);
        result.put("totalCount", totalCount);
        result.put("successRate", successRate);

        return ResponseEntity.ok(result);
    }
}