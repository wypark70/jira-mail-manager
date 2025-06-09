package com.samsungds.ims.mail.component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mail.batch.move-to-history")
@RefreshScope
@Getter
@Setter
@ToString
public class MoveToHistoryBatchProperties {
    private String scheduleCron = "0 0/5 * * * ?";
}
