package com.example.scrumio.web.dto;

import java.time.Instant;
import java.util.UUID;

public interface TicketNativeProjection {
    UUID getId();
    String getTitle();
    String getDescription();
    String getPriority();
    String getStatus();
    Integer getEstimation();
    UUID getSprintId();
    String getSprintName();
    UUID getProjectId();
    Instant getCreatedAt();
}
