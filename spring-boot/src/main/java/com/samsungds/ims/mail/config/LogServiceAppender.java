package com.samsungds.ims.mail.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.samsungds.ims.mail.dto.LogMessage;
import com.samsungds.ims.mail.service.EmailQueueBatchLogService;

public class LogServiceAppender extends AppenderBase<ILoggingEvent> {
    private EmailQueueBatchLogService emailQueueBatchLogService;

    public void setLogService(EmailQueueBatchLogService emailQueueBatchLogService) {
        this.emailQueueBatchLogService = emailQueueBatchLogService;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (emailQueueBatchLogService != null) {
            String level = event.getLevel().toString();
            String message = event.getFormattedMessage();
            LogMessage logMessage = new LogMessage(message, level);
            emailQueueBatchLogService.addLog(logMessage);
        }
    }
}