package com.samsungds.ims.mail.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.samsungds.ims.mail.appender.LogServiceAppender;
import com.samsungds.ims.mail.service.LogStreamService;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.List;

@Configuration
public class LogConfig {
    private final LogStreamService logStreamService;

    public LogConfig(LogStreamService logStreamService) {
        this.logStreamService = logStreamService;
    }

    @PostConstruct
    public void init() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<Logger> loggerList = loggerContext.getLoggerList();

        loggerList.forEach(logger -> {
            Iterator<Appender<ILoggingEvent>> appenderIterator = logger.iteratorForAppenders();
            while(appenderIterator.hasNext()) {
                Appender<ILoggingEvent> appender = appenderIterator.next();
                if (appender instanceof LogServiceAppender) {
                    ((LogServiceAppender) appender).setLogService(logStreamService);
                }
            }
        });
    }
}
