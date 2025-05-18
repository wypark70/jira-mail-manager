package com.samsungds.ims.mail.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * LogMessage DTO for logging purposes.
 * This class is used to encapsulate log messages with a timestamp and log level.
 */
@Getter
@Setter
public class LogMessage {
    private String message;
    private LocalDateTime timestamp;
    private String level;

    public LogMessage(String message, String level) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.level = level;
    }
}