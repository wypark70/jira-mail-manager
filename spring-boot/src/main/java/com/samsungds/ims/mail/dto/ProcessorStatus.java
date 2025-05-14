package com.samsungds.ims.mail.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProcessorStatus {
    private final String processorId;
    private final boolean running;
    private final LocalDateTime startedAt;
}