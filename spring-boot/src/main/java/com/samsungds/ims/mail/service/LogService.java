package com.samsungds.ims.mail.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import org.springframework.stereotype.Service;

import com.samsungds.ims.mail.dto.LogMessage;

@Service
public class LogService {
    private final Sinks.Many<LogMessage> logSink;
    
    public LogService() {
        this.logSink = Sinks.many().multicast().onBackpressureBuffer();
    }
    
    public void sendLog(String message, String level) {
        LogMessage logMessage = new LogMessage(message, level);
        logSink.tryEmitNext(logMessage);
    }
    
    public Flux<LogMessage> getLogStream() {
        return logSink.asFlux();
    }
}