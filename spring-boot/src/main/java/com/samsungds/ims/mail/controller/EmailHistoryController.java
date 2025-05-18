package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.model.EmailHistory;
import com.samsungds.ims.mail.model.EmailQueue;
import com.samsungds.ims.mail.repository.EmailHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/email-history")
@RequiredArgsConstructor
@Slf4j
public class EmailHistoryController {
    private final EmailHistoryRepository emailHistoryRepository;

    /**
     * 이메일 이력 상세 정보 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmailHistory> getEmailDetails(@PathVariable Long id) {
        Optional<EmailHistory> emailQueue = emailHistoryRepository.findById(id);
        return emailQueue.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 이메일 이력 검색 및 필터링
     */
    @SuppressWarnings("DuplicatedCode")
    @GetMapping("/search")
    public ResponseEntity<Page<EmailHistory>> searchEmails(
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

            Page<EmailHistory> emails = emailHistoryRepository.findByStatusAndSubjectAndSentAtBetween(
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
}
