package com.samsungds.ims.mail.component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mail.batch.send-mail")
@RefreshScope
@Getter
@Setter
@ToString
public class SendMailBatchProperties {
    private int batchSize = 10;
    private int concurrentBatchSize = 5;
    private int lockTimeoutMinutes = 1;
    private int retryDelayMinutes = 1;
    private String scheduleCron = "0 0/1 * * * ?";
    private String knoxApiUrl = "https://knox-api.com";
    private String knoxApiKey = "knox-api-key";
    private String knoxApiSecret = "knox-api-secret";
}
