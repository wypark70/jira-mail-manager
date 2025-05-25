package com.samsungds.ims.mail;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SmtpMailTests {
    private static final String SMTP_HOST = "localhost";
    private static final int SMTP_PORT = 25;

    @Test
    void TestSend() {
        try {
            for (int i = 0; i < 500; i++) {
                // 테스트 이메일 발송
                String subject = getRandomSubject();
                String sender = getRandomSender();
                String recipient = getRandomRecipient();
                String body = subject + "\n" + sender + "\n" + recipient;
                sendTestEmail(sender, recipient, subject, body);
            }
        } catch (MessagingException e) {
            System.out.println("이메일 발송 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

        return sampleSubjects[(int) (Math.random() * sampleSubjects.length)];
    }

    private String getRandomSender() {
        String[] sampleSenders = {
                "tech.lead@company.com",
                "project.manager@company.com",
                "system.admin@company.com",
                "dev.team@company.com",
                "security.officer@company.com",
                "qa.manager@company.com",
                "support.team@company.com",
                "infra.team@company.com",
                "cloud.architect@company.com",
                "data.analyst@company.com",
                "frontend.dev@company.com",
                "backend.dev@company.com",
                "devops.engineer@company.com",
                "hr.team@company.com",
                "finance.dept@company.com",
                "marketing.team@company.com",
                "sales.dept@company.com",
                "research.team@company.com",
                "product.owner@company.com",
                "scrum.master@company.com",
                "ui.designer@company.com",
                "ux.researcher@company.com",
                "mobile.dev@company.com",
                "database.admin@company.com",
                "network.engineer@company.com",
                "security.team@company.com",
                "compliance.officer@company.com",
                "it.support@company.com",
                "api.team@company.com",
                "testing.team@company.com"
        };

        return sampleSenders[(int) (Math.random() * sampleSenders.length)];
    }

    private String getRandomRecipient() {
        String[] sampleRecipients = {
                // 개발팀
                "dev.kim@company.com",
                "dev.lee@company.com",
                "dev.park@company.com",
                "dev.choi@company.com",
                "dev.jung@company.com",

                // QA팀
                "qa.hong@company.com",
                "qa.kang@company.com",
                "qa.yoon@company.com",
                "qa.shin@company.com",
                "qa.han@company.com",

                // 운영팀
                "ops.lim@company.com",
                "ops.song@company.com",
                "ops.jang@company.com",
                "ops.kwon@company.com",
                "ops.cho@company.com",

                // 기획팀
                "plan.kim@company.com",
                "plan.lee@company.com",
                "plan.park@company.com",
                "plan.jung@company.com",
                "plan.ahn@company.com",

                // 디자인팀
                "design.oh@company.com",
                "design.seo@company.com",
                "design.yang@company.com",
                "design.bae@company.com",
                "design.jeon@company.com",

                // 마케팅팀
                "mkt.hwang@company.com",
                "mkt.yoo@company.com",
                "mkt.chung@company.com",
                "mkt.moon@company.com",
                "mkt.nam@company.com",

                // 영업팀
                "sales.ko@company.com",
                "sales.ryu@company.com",
                "sales.baek@company.com",
                "sales.kwak@company.com",
                "sales.ku@company.com",

                // 인사팀
                "hr.han@company.com",
                "hr.im@company.com",
                "hr.sung@company.com",
                "hr.gang@company.com",
                "hr.jin@company.com",

                // 경영지원팀
                "mgmt.son@company.com",
                "mgmt.cho@company.com",
                "mgmt.kook@company.com",
                "mgmt.yoon@company.com",
                "mgmt.ha@company.com",

                // 연구소
                "lab.joo@company.com",
                "lab.bong@company.com",
                "lab.woo@company.com",
                "lab.min@company.com",
                "lab.suh@company.com"
        };

        return sampleRecipients[(int) (Math.random() * sampleRecipients.length)];
    }

    @Test
    void testSend2() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("admin"); // 예제 비밀번호
        System.out.println("[" + encodedPassword + "]" );
    }
}
