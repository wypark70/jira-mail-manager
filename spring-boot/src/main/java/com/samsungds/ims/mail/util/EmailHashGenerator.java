package com.samsungds.ims.mail.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat; // Java 17 이상에서 사용 가능

public class EmailHashGenerator {

    /**
     * 주어진 문자열(메일 본문)로부터 SHA-256 해시 값을 생성합니다.
     *
     * @param emailBody 해시 값을 생성할 메일 본문 문자열
     * @return 생성된 SHA-256 해시 값 (16진수 문자열), 또는 입력이 null이면 null
     */
    public static String generateSha256Hash(String emailBody) {
        if (emailBody == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    emailBody.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256은 표준 알고리즘이므로 이 예외는 거의 발생하지 않습니다.
            // 실제 애플리케이션에서는 적절한 로깅 및 예외 처리가 필요합니다.
            // 여기서는 간단히 RuntimeException으로 처리합니다.
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환합니다.
     *
     * @param hash 변환할 바이트 배열
     * @return 16진수 문자열
     */
    private static String bytesToHex(byte[] hash) {
        // Java 17 이상인 경우 HexFormat 사용
        if (isJava17OrLater()) {
            return HexFormat.of().formatHex(hash);
        } else {
            // 이전 Java 버전의 경우 수동 변환
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
    }

    private static boolean isJava17OrLater() {
        try {
            // HexFormat 클래스가 있는지 확인하여 Java 17 이상인지 간접적으로 판단
            Class.forName("java.util.HexFormat");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // 예제 사용법
    public static void main(String[] args) {
        String htmlBody1 = "<p>안녕하세요! <strong>Jira 이슈 알림</strong>입니다.</p><div>새로운 댓글이 추가되었습니다.</div>";
        String htmlBody2 = "<p>안녕하세요! <strong>Jira 이슈 알림</strong>입니다.</p><div>새로운 댓글이 추가되었습니다.</div>"; // htmlBody1과 동일
        String htmlBody3 = "<p>안녕하세요! <strong>Jira 이슈 알림</strong>입니다.</p><div>내용이 약간 다릅니다.</div>";

        String textBody1 = "안녕하세요! Jira 이슈 알림입니다.\n새로운 댓글이 추가되었습니다.";

        System.out.println("HTML Body 1 Hash: " + generateSha256Hash(htmlBody1));
        System.out.println("HTML Body 2 Hash: " + generateSha256Hash(htmlBody2)); // htmlBody1 해시와 동일
        System.out.println("HTML Body 3 Hash: " + generateSha256Hash(htmlBody3));
        System.out.println("Text Body 1 Hash: " + generateSha256Hash(textBody1));
        System.out.println("Null Body Hash: " + generateSha256Hash(null));
    }
}
