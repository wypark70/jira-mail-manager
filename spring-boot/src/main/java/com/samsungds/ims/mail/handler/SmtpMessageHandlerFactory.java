package com.samsungds.ims.mail.handler;

import com.samsungds.ims.mail.repository.EmailQueueRepository;
import com.samsungds.ims.mail.repository.EmailRecipientRepository;
import com.samsungds.ims.mail.repository.EmailContentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class SmtpMessageHandlerFactory implements MessageHandlerFactory {

    private final EmailQueueRepository emailQueueRepository;
    private final EmailRecipientRepository emailRecipientRepository;
    private final EmailContentRepository emailContentRepository;
    @Value("${mail.smtp.allowed.ips:127.0.0.1}")
    private List<String> allowedIps; // 허용된 IP 목록
    @Value("${mail.smtp.allowed.domains:localhost}")
    private List<String> allowedDomains; // 허용된 도메인 목록

    @PostConstruct
    public void init() {
        log.info("===== SMTP 서버 설정 초기화 =====");
        log.info("허용된 IP 목록: {}", allowedIps);
        if (allowedDomains != null) {
            log.info("허용된 도메인 목록: {}", allowedDomains);
        }
        log.info("==============================");
    }

    @Override
    public MessageHandler create(MessageContext ctx) {
        log.info("allowedIps: {}", allowedIps);
        log.info("allowedDomains: {}", allowedDomains);

        String clientIp = getIpAddress(ctx.getRemoteAddress());
        String domain = getDomainFromIp(clientIp);

        // IP와 도메인 필터링 로직
        if (!isAllowedConnection(clientIp, domain)) {
            log.info("차단된 클라이언트 IP: {}, 도메인: {}", clientIp, domain);
            throw new RuntimeException("허용되지 않은 클라이언트 IP 또는 도메인: " + clientIp + " (" + domain + ")");
        }

        // 연결이 허용된 경우 메시지 핸들러 반환
        log.info("허용된 클라이언트 IP: {}, 도메인: {}", clientIp, domain);
        return new SmtpMessageHandler(
            emailQueueRepository,
            emailRecipientRepository,
            emailContentRepository
        );
    }

    /**
     * SocketAddress에서 IP 주소를 추출하는 메서드
     */
    public String getIpAddress(SocketAddress socketAddress) {
        // InetSocketAddress로 캐스팅
        if (socketAddress instanceof InetSocketAddress inetSocketAddress) {

            // InetAddress 가져오기
            return inetSocketAddress.getAddress().getHostAddress(); // IP 주소 반환
        }

        // 잘못된 타입일 경우 예외 처리
        throw new IllegalArgumentException("InetSocketAddress가 아닌 SocketAddress 타입입니다.");
    }

    /**
     * IP 주소에서 도메인 이름을 가져오는 메서드
     */
    private String getDomainFromIp(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            String hostname = inetAddress.getHostName();

            // IP와 hostname이 같으면 역방향 DNS 조회가 실패한 것이므로 도메인이 없음
            if (hostname.equals(ipAddress)) {
                return "unknown";
            }

            return hostname;
        } catch (UnknownHostException e) {
            log.info("도메인 조회 실패: {}", ipAddress);
            return "unknown";
        }
    }

    /**
     * IP 또는 도메인이 허용되었는지 확인
     */
    private boolean isAllowedConnection(String clientIp, String domain) {
        return allowedIps.contains(clientIp) || allowedDomains.stream().anyMatch(domain::endsWith);
    }
}