package com.samsungds.ims.mail.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AllowDomainFilter {
    @Value("${mail.smtp.allowed.ips:127.0.0.1}")
    private List<String> allowedIps; // 허용된 IP 목록
    @Value("${mail.smtp.allowed.domains:localhost}")
    private List<String> allowedDomains; // 허용된 도메인 목록
    @Value("${mail.smtp.allowed.email-domain:samsung.com}")
    private List<String> allowedEmailDomains; // 허용된 이메일 도메인

    public boolean isAllowedIp(String ip) {
        return allowedIps.contains(ip);
    }

    public boolean isAllowedDomain(String domain) {
        return allowedDomains.stream().anyMatch(domain::endsWith);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isAllowedEmailDomain(String emailDomain) {
        return allowedEmailDomains.stream().anyMatch(emailDomain::endsWith);
    }

}
