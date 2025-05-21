package com.samsungds.ims.mail.controller;

import com.samsungds.ims.mail.dto.ProcessorStatus;
import com.samsungds.ims.mail.service.SendToSmtpRequestBatchService;
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
@RequestMapping("/api/send-to-smtp-request-batch")
@RequiredArgsConstructor
@Slf4j
public class SendToSmtpRequestBatchController {
    private final SendToSmtpRequestBatchService sendToSmtpRequestBatchService;

    /**
     * SMTP 요청 보내기 배치 시작
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startProcessor() {
        Map<String, Object> response = new HashMap<>();

        if (sendToSmtpRequestBatchService.isRunning()) {
            response.put("success", false);
            response.put("message", "SMTP 요청 보내기 배치가 이미 실행 중입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            sendToSmtpRequestBatchService.start();
            ProcessorStatus status = sendToSmtpRequestBatchService.getProcessorStatus();

            response.put("success", true);
            response.put("message", "SMTP 요청 보내기 배치가 시작되었습니다.");
            response.put("processorId", status.getProcessorId());
            response.put("startedAt", status.getStartedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "SMTP 요청 보내기 배치 시작 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * SMTP 요청 보내기 배치 종료
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopProcessor() {
        Map<String, Object> response = new HashMap<>();

        if (!sendToSmtpRequestBatchService.isRunning()) {
            response.put("success", false);
            response.put("message", "SMTP 요청 보내기 배치가 이미 중지된 상태입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            sendToSmtpRequestBatchService.stop();

            response.put("success", true);
            response.put("message", "SMTP 요청 보내기 배치가 중지되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "SMTP 요청 보내기 배치 중지 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * SMTP 요청 보내기 배치 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<ProcessorStatus> getProcessorStatus() {
        ProcessorStatus status = sendToSmtpRequestBatchService.getProcessorStatus();
        return ResponseEntity.ok(status);
    }

}
