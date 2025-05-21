package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.dto.ProcessorStatus;
import com.samsungds.ims.mail.service.MoveToHistoryBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/move-to-history-batch")
@RequiredArgsConstructor
@Slf4j
public class MoveToHistoryBatchController {
    private final MoveToHistoryBatchService moveToHistoryBatchService;

    /**
     * 히스토리 테이블 이동 배치 시작
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startProcessor() {
        Map<String, Object> response = new HashMap<>();

        if (moveToHistoryBatchService.isRunning()) {
            response.put("success", false);
            response.put("message", "히스토리 테이블 이동 배치가 이미 실행 중입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            moveToHistoryBatchService.start();
            ProcessorStatus status = moveToHistoryBatchService.getProcessorStatus();

            response.put("success", true);
            response.put("message", "히스토리 테이블 이동 배치가 시작되었습니다.");
            response.put("processorId", status.getProcessorId());
            response.put("startedAt", status.getStartedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "히스토리 테이블 이동 배치 시작 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 히스토리 테이블 이동 배치 종료
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopProcessor() {
        Map<String, Object> response = new HashMap<>();

        if (!moveToHistoryBatchService.isRunning()) {
            response.put("success", false);
            response.put("message", "히스토리 테이블 이동 배치가 이미 중지된 상태입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            moveToHistoryBatchService.stop();

            response.put("success", true);
            response.put("message", "히스토리 테이블 이동 배치가 중지되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "히스토리 테이블 이동 배치 중지 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 히스토리 테이블 이동 배치 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<ProcessorStatus> getProcessorStatus() {
        ProcessorStatus status = moveToHistoryBatchService.getProcessorStatus();
        return ResponseEntity.ok(status);
    }

}
