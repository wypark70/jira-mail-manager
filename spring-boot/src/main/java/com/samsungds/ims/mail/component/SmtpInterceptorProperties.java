package com.samsungds.ims.mail.component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "mail.smtp-interceptor")
@RefreshScope
@Getter
@Setter
@ToString
public class SmtpInterceptorProperties {
    private int port = 25;
    private List<String> allowedIps = new ArrayList<>(List.of("127.0.0.1"));;
    private List<String> allowedDomains = new ArrayList<>(List.of("localhost"));;
    private List<String> allowedEmailDomains = new ArrayList<>(List.of("samsung.com", "partner.samsung.com"));
    private String attachmentPath = "./attachments";
    private String userApiUrl = "http://localhost:2990/jira/rest/api/2/user/search";
    private String userApiUsername = "admin";
    private String userApiPassword = "admin";

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
