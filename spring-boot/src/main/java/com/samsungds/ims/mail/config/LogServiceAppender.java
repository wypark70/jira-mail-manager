package com.samsungds.ims.mail.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.samsungds.ims.mail.dto.LogMessage;
import com.samsungds.ims.mail.service.RootLogStreamService;

public class LogServiceAppender extends AppenderBase<ILoggingEvent> {
    private RootLogStreamService rootLogStreamService;

    public void setLogService(RootLogStreamService rootLogStreamService) {
        this.rootLogStreamService = rootLogStreamService;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (rootLogStreamService != null) {
            String level = event.getLevel().toString();
            String message = event.getFormattedMessage();
            LogMessage logMessage = new LogMessage(message, level);
            rootLogStreamService.addLog(logMessage);
        }
    }
}