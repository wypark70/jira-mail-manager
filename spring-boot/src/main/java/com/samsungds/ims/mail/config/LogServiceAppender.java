package com.samsungds.ims.mail.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.samsungds.ims.mail.service.LogService;

public class LogServiceAppender extends AppenderBase<ILoggingEvent> {
    private LogService logService;

    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (logService != null) {
            String level = event.getLevel().toString();
            String message = event.getFormattedMessage();
            logService.sendLog(message, level);
        }
    }
}