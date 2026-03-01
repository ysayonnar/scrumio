package com.example.scrumio.web.dto;

import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        Integer estimation,
        UUID sprintId,
        UUID projectId,
        OffsetDateTime createdAt
) {
}
