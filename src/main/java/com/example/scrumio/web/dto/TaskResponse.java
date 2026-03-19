package com.example.scrumio.web.dto;

import com.example.scrumio.concurrency.TaskStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID taskId,
        TaskStatus status,
        String result,
        OffsetDateTime createdAt
) {
}
