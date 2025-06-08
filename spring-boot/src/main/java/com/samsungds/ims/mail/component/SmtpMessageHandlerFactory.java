package com.samsungds.ims.mail.component;

import com.samsungds.ims.mail.repository.EmailQueueAttachmentRepository;
import com.samsungds.ims.mail.repository.EmailQueueContentRepository;
import com.samsungds.ims.mail.repository.EmailQueueRecipientRepository;
import com.samsungds.ims.mail.repository.EmailQueueRepository;
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

@RequiredArgsConstructor
@Component
@Slf4j
public class SmtpMessageHandlerFactory implements MessageHandlerFactory {

    private final EmailQueueRepository emailQueueRepository;
    private final EmailQueueRecipientRepository emailQueueRecipientRepository;
    private final EmailQueueContentRepository emailQueueContentRepository;
    private final EmailQueueAttachmentRepository emailQueueAttachmentRepository;
    private final MailSmtpProperties mailSmtpProperties;

    @Value("${mail.smtp.attachment-path:./attachments}")
    private String attachmentStoragePath;

    @PostConstruct
    public void init() {
        log.info("SmtpMessageHandlerFactory initialized. Attachment storage path: {}", attachmentStoragePath);
    }

    @Override
    public MessageHandler create(MessageContext ctx) {
        String clientIp = getIpAddress(ctx.getRemoteAddress());
        String domain = getDomainFromIp(clientIp);

        // IP와 도메인 필터링 로직
        if (!isAllowedConnection(clientIp, domain)) {
            log.info("차단된 클라이언트 IP: {}, 도메인: {}", clientIp, domain);
            throw new SecurityException("허용되지 않은 클라이언트 IP 또는 도메인으로부터의 연결 시도: " + clientIp + " (" + domain + ")");
        }

        // 연결이 허용된 경우 메시지 핸들러 반환
        log.info("허용된 클라이언트 IP: {}, 도메인: {}", clientIp, domain);
        return new SmtpMessageHandler(emailQueueRepository, emailQueueRecipientRepository, emailQueueContentRepository, emailQueueAttachmentRepository, mailSmtpProperties);
    }

    /**
     * SocketAddress에서 IP 주소를 추출하는 메서드
     */
    private String getIpAddress(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress inetSocketAddress) {
            return inetSocketAddress.getAddress().getHostAddress();
        }
        // 지원되지 않는 SocketAddress 타입인 경우, 로그를 남기고 예외 발생
        String actualType = (socketAddress != null) ? socketAddress.getClass().getName() : "null";
        log.warn("지원되지 않는 SocketAddress 타입입니다: {}", actualType);
        throw new IllegalArgumentException("SocketAddress는 InetSocketAddress 타입이어야 합니다. 실제 타입: " + actualType);
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
            log.warn("IP 주소 '{}'에 대한 도메인 조회 중 오류 발생: {}", ipAddress, e.getMessage());
            return "unknown";
        }
    }

    /**
     * IP 또는 도메인이 허용되었는지 확인
     */
    private boolean isAllowedConnection(String clientIp, String domain) {
        return mailSmtpProperties.isAllowedIp(clientIp) || mailSmtpProperties.isAllowedDomain(domain);
    }
}