package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.dto.ProcessorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendToSmtpRequestBatchService implements SmartLifecycle {
    private static final String SMTP_HOST = "localhost";
    private static final int SMTP_PORT = 25;

    private final String processorId = generateProcessorId();
    private volatile boolean running = false;
    private final Random random = new Random();

    // processorId 생성
    private String generateProcessorId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-"
                    + ProcessHandle.current().pid() + "-"
                    + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    // SmartLifecycle 구현
    @Override
    public void start() {
        log.info("SMTP 요청 보내기 배치 시작 - 프로세서 ID: {}", processorId);
        running = true;
    }

    @Override
    public void stop() {
        log.info("SMTP 요청 보내기 배치 종료 - 프로세서 ID: {}", processorId);
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return SmartLifecycle.super.isAutoStartup();
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return 0;
    }

    /**
     * SMTP 요청 보내기 (1분 마다 실행)
     */
    @Scheduled(cron = "0 */1 * * * *")
    public void processSendToSmtpRequest() {
        if (!running) {
            log.debug("SMTP 요청 보내기 배치가 실행 중이 아닙니다.");
            return;
        }
        log.info("SMTP 요청 보내기 배치 시작, 프로세서 ID: {}", processorId);
        try {
            // 10 ~ 50개의 메일을 발송
            int randomNumber = random.nextInt(31) + 20;
            for (int i = 0; i < randomNumber; i++) {
                String from = getRandomSender();
                String to = getRandomRecipient();
                String subject = getRandomSubject();
                String body = "<h4>이메일 보내기 테스트입니다.</h4>\n<p>from: " + from + "</p>\n<p>to: " + to + "</p>\n<p>subject: " + subject + "</p>\n";
                sendTestEmail(from, to, subject, body);
            }
            log.info("SMTP 요청 보내기 배치 처리 완료");
        } catch (Exception e) {
            log.error("SMTP 요청 보내기 배치 처리 중 오류 발생", e);
        }
    }

    /**
     * 배치 프로세서의 현재 상태 조회
     */
    public ProcessorStatus getProcessorStatus() {
        return ProcessorStatus.builder()
                .processorId(processorId)
                .running(running)
                .startedAt(LocalDateTime.now())
                .build();
    }

    private void sendTestEmail(String from, String to, String subject, String body)
            throws MessagingException, InterruptedException {
        // SMTP 설정
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        // 세션 생성
        Session session = Session.getInstance(props);
        session.setDebug(true); // 디버그 모드 활성화

        // 메시지 생성
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        // 메일 전송
        Transport.send(message);
        Thread.sleep(300);
        System.out.println("이메일이 성공적으로 전송되었습니다.");
    }


    private String getRandomSubject() {
        String[] sampleSubjects = {
                "[공지] 2024년 상반기 보안 교육 안내",
                "[긴급] 시스템 점검 예정 안내",
                "[알림] 주간 회의 일정 변경",
                "[공지] 신규 프로젝트 킥오프 미팅",
                "[안내] 연간 휴가 사용 안내",
                "[요청] 프로젝트 상태 보고서 제출",
                "[공유] API 문서 업데이트 안내",
                "[안내] 신규 보안 정책 시행",
                "[공지] 사내 네트워크 업그레이드 작업",
                "[요청] 코드 리뷰 요청",
                "[공유] 스프린트 회고 미팅 결과",
                "[알림] 데이터베이스 마이그레이션 일정",
                "[긴급] 서버 장애 복구 완료 보고",
                "[공지] 신규 직원 교육 일정",
                "[안내] 연말 정산 자료 제출",
                "[요청] 성과 평가 자료 작성",
                "[공유] 팀 빌딩 일정 안내",
                "[알림] 소프트웨어 라이선스 갱신",
                "[공지] 사내 시스템 업데이트",
                "[요청] 월간 보고서 작성",
                "[공유] 신규 기능 배포 안내",
                "[안내] 재택근무 가이드라인",
                "[긴급] 보안 패치 적용 요청",
                "[공지] 연간 목표 설정 미팅",
                "[알림] 프로젝트 마일스톤 변경",
                "[요청] 테스트 케이스 리뷰",
                "[공유] 기술 세미나 일정",
                "[안내] 연차 사용 계획 제출",
                "[공지] 분기별 성과 미팅",
                "[긴급] 클라우드 서비스 장애 공지",
                "[알림] 팀 워크숍 일정",
                "[요청] 코드 품질 개선 계획",
                "[공유] 신규 도구 도입 안내",
                "[안내] 개인정보 보호 교육",
                "[공지] 사내 인프라 개선 작업",
                "[긴급] 보안 취약점 패치 안내",
                "[알림] 프로젝트 일정 조정",
                "[요청] 기술 문서 검토",
                "[공유] 팀 미팅 안건",
                "[안내] 휴가 신청 절차 변경",
                "[공지] IT 자산 실사 안내",
                "[긴급] 시스템 백업 완료 보고",
                "[알림] 외부 감사 일정",
                "[요청] 업무 프로세스 개선안",
                "[공유] 신규 서비스 론칭 안내",
                "[안내] 사내 교육 프로그램",
                "[공지] 분기별 예산 검토",
                "[긴급] 네트워크 보안 점검",
                "[알림] 연말 행사 일정",
                "[요청] 프로젝트 리소스 검토"
        };

        return sampleSubjects[random.nextInt(sampleSubjects.length)];
    }

    private String getRandomDomain() {
        String[] domains = {
                "samsung.com",
                "partner.samsung.com",
                "partner.sec.co.kr",
                "naver.com",
                "google.com"
        };
        return domains[random.nextInt(domains.length)];
    }

    private String getRandomSender() {
        String[] ids = {
                "tech.lead",
                "project.manager",
                "system.admin",
                "dev.team",
                "security.officer",
                "qa.manager",
                "support.team",
                "infra.team",
                "cloud.architect",
                "data.analyst",
                "frontend.dev",
                "backend.dev",
                "devops.engineer",
                "hr.team",
                "finance.dept",
                "marketing.team",
                "sales.dept",
                "research.team",
                "product.owner",
                "scrum.master",
                "ui.designer",
                "ux.researcher",
                "mobile.dev",
                "database.admin",
                "network.engineer",
                "security.team",
                "compliance.officer",
                "it.support",
                "api.team",
                "testing.team"
        };

        return ids[random.nextInt(ids.length)] + "@" + getRandomDomain();
    }

    private String getRandomRecipient() {
        String[] ids = {
                // 개발팀
                "dev.kim",
                "dev.lee",
                "dev.park",
                "dev.choi",
                "dev.jung",

                // QA팀
                "qa.hong",
                "qa.kang",
                "qa.yoon",
                "qa.shin",
                "qa.han",

                // 운영팀
                "ops.lim",
                "ops.song",
                "ops.jang",
                "ops.kwon",
                "ops.cho",

                // 기획팀
                "plan.kim",
                "plan.lee",
                "plan.park",
                "plan.jung",
                "plan.ahn",

                // 디자인팀
                "design.oh",
                "design.seo",
                "design.yang",
                "design.bae",
                "design.jeon",

                // 마케팅팀
                "mkt.hwang",
                "mkt.yoo",
                "mkt.chung",
                "mkt.moon",
                "mkt.nam",

                // 영업팀
                "sales.ko",
                "sales.ryu",
                "sales.baek",
                "sales.kwak",
                "sales.ku",

                // 인사팀
                "hr.han",
                "hr.im",
                "hr.sung",
                "hr.gang",
                "hr.jin",

                // 경영지원팀
                "mgmt.son",
                "mgmt.cho",
                "mgmt.kook",
                "mgmt.yoon",
                "mgmt.ha",

                // 연구소
                "lab.joo",
                "lab.bong",
                "lab.woo",
                "lab.min",
                "lab.suh"
        };

        return ids[random.nextInt(ids.length)] + "@" + getRandomDomain();
    }
}