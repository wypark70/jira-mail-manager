package com.samsungds.ims.mail.component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mail.batch")
@RefreshScope
@Getter
@Setter
@ToString
public class MailBatchProperties {
    private int batchSize = 10;
    private int concurrentBatchSize = 5;
    private int lockTimeoutMinutes = 1;
    private int retryDelayMinutes = 1;
    private String queueProcessingCron = "0 0/1 * * * ?";
    private String moveToHistoryCron = "0 0/5 * * * ?";
}
