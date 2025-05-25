package com.samsungds.ims.mail.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.samsungds.ims.mail.dto.LogMessage;
import com.samsungds.ims.mail.service.LogStreamService;

public class LogServiceAppender extends AppenderBase<ILoggingEvent> {
    private LogStreamService logStreamService;

    public void setLogService(LogStreamService logStreamService) {
        this.logStreamService = logStreamService;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (logStreamService != null) {
            String level = event.getLevel().toString();
            String message = event.getFormattedMessage();
            LogMessage logMessage = new LogMessage(message, level);
            logStreamService.addLog(logMessage);
        }
    }
}