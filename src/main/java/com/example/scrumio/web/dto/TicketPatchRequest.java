package com.example.scrumio.web.dto;

import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;

import java.util.UUID;

public record TicketPatchRequest(
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        Integer estimation,
        UUID sprintId
) {
}
