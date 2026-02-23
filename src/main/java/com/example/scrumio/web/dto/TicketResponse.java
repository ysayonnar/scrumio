package com.example.scrumio.web.dto;

import com.example.scrumio.entity.TicketPriority;
import com.example.scrumio.entity.TicketStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        int estimation,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt,

        @JsonProperty("deleted_at")
        LocalDateTime deletedAt,

        @JsonProperty("sprint_id")
        UUID sprintID
) {
}
