package com.samsungds.ims.mail.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.samsungds.ims.mail.service.EmailQueueBatchLogService;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;

@Configuration
public class LogConfig {
    private final EmailQueueBatchLogService emailQueueBatchLogService;

    public LogConfig(EmailQueueBatchLogService emailQueueBatchLogService) {
        this.emailQueueBatchLogService = emailQueueBatchLogService;
    }

    @PostConstruct
    public void init() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        Iterator<Appender<ILoggingEvent>> appenderIterator = rootLogger.iteratorForAppenders();
        while(appenderIterator.hasNext()) {
            Appender<ILoggingEvent> appender = appenderIterator.next();
            if (appender instanceof LogServiceAppender) {
                ((LogServiceAppender) appender).setLogService(emailQueueBatchLogService);
            }
        }
    }
}