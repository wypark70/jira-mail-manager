package com.samsungds.ims.mail;

import org.junit.jupiter.api.Test;

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
            for (int i = 0; i < 200; i++) {
                // 테스트 이메일 발송
                sendTestEmail(
                        "sender" + i + "@example.com",
                        "recipient" + i + "@example.com",
                        "테스트 이메일 #" + i,
                        "이것은 테스트 이메일입니다.\n" + i + "번째 메일입니다.");
            }
            for (int i = 0; i < 200; i++) {
                // 테스트 이메일 발송
                sendTestEmail(
                        "sender2" + i + "@example.com",
                        "recipient2" + i + "@example.com",
                        "테스트 이메일 #2" + i,
                        "이것은 테스트 이메일입니다.\n" + i + "번째 메일입니다.");
            }
        } catch (MessagingException e) {
            System.out.println("이메일 발송 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendTestEmail(String from, String to, String subject, String body)
            throws MessagingException {
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
        System.out.println("이메일이 성공적으로 전송되었습니다.");
    }
}
