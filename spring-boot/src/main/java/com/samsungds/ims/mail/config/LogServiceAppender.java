package com.samsungds.ims.mail.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.samsungds.ims.mail.dto.LogMessage;
import com.samsungds.ims.mail.service.EmailQueueProcessLogService;

public class LogServiceAppender extends AppenderBase<ILoggingEvent> {
    private EmailQueueProcessLogService emailQueueProcessLogService;

    public void setLogService(EmailQueueProcessLogService emailQueueProcessLogService) {
        this.emailQueueProcessLogService = emailQueueProcessLogService;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (emailQueueProcessLogService != null) {
            String level = event.getLevel().toString();
            String message = event.getFormattedMessage();
            LogMessage logMessage = new LogMessage(message, level);
            emailQueueProcessLogService.addLog(logMessage);
        }
    }
}