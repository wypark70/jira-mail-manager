package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.dto.EmailQueueStats;
import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.repository.EmailQueueRepository;
import com.samsungds.ims.mail.service.EmailHistoryService;
import com.samsungds.ims.mail.service.EmailQueueService;
import com.samsungds.ims.mail.service.EmailQueueStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/email-queue")
@RequiredArgsConstructor
@Slf4j
public class EmailQueueController {
    private final EmailQueueRepository emailQueueRepository;
    private final EmailHistoryService emailHistoryService;
    private final EmailQueueService emailQueueService;
    private final EmailQueueStreamService emailQueueStreamService;

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
    public ResponseEntity<EmailQueueStats> getQueueStats() {
        EmailQueueStats stats = emailQueueService.getQueueStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 실시간 통계 스트림 제공
     */
    @GetMapping(path = "/stats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStats() {
        return emailQueueStreamService.streamStats();
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
     * 실패한 전체 이메일 재시도 큐에 추가
     */
    @PostMapping("/retry")
    public ResponseEntity<Map<String, Object>> retryFailedEmail() {
        List<EmailQueue> emailQueueList = emailQueueRepository.findByStatus(EmailQueue.EmailStatus.FAILED);

        if (emailQueueList.isEmpty()) {
            Map<String, Object> failResponse = new HashMap<>();
            failResponse.put("success", false);
            failResponse.put("message", "실패한 이메일이 없습니다.");
            return ResponseEntity.ok(failResponse);
        }

        emailQueueList.forEach(emailQueue -> {
            emailQueue.setStatus(EmailQueue.EmailStatus.QUEUED);
            emailQueue.setRetryCount(0);
            emailQueue.setErrorMessage(null);
            emailQueue.setUpdatedAt(LocalDateTime.now());
            emailQueueRepository.save(emailQueue);
        });

        // 재시도 설정
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", "이메일 재시도가 큐에 추가되었습니다.");

        return ResponseEntity.ok(successResponse);
    }

    /**
     * 이메일 상세 정보 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmailQueue> getEmailDetails(@PathVariable Long id) {
        Optional<EmailQueue> emailQueue = emailQueueRepository.findById(id);
        return emailQueue.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 이메일 검색 및 필터링
     */
    @SuppressWarnings("DuplicatedCode")
    @GetMapping("/search")
    public ResponseEntity<Page<EmailQueue>> searchEmails(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        try {
            EmailQueue.EmailStatus emailStatus = status != null && !status.isEmpty() ? EmailQueue.EmailStatus.valueOf(status.toUpperCase()) : null;

            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            // startDate가 있으면 시작시간을 해당 날짜의 00:00:00으로 설정
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;

            // endDate가 있으면 종료시간을 해당 날짜의 23:59:59로 설정
            LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

            Page<EmailQueue> emails = emailQueueRepository.findByStatusAndSubjectAndCreatedAtBetween(
                    emailStatus,
                    subject,
                    start,
                    end,
                    pageable
            );

            return ResponseEntity.ok(emails);
        } catch (IllegalArgumentException e) {
            log.error("검색 파라미터 오류", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/move-to-history/{status}")
    public ResponseEntity<String> moveToHistory(@PathVariable EmailQueue.EmailStatus status) {
        try {
            int movedCount = emailHistoryService.moveAllEmailsToHistoryByStatus(status);
            return ResponseEntity.ok(String.format("성공적으로 %d개의 이메일을 히스토리로 이동했습니다.", movedCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이메일 히스토리 이동 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}