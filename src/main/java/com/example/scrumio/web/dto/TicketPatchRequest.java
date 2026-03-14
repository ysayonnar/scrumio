package com.example.scrumio.web.dto;

import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Request payload for partially updating a ticket")
public record TicketPatchRequest(
        @Schema(description = "Ticket title", example = "Fix login bug") String title,
        @Schema(description = "Ticket description") String description,
        @Schema(description = "Ticket priority", example = "MEDIUM") TicketPriority priority,
        @Schema(description = "Ticket status", example = "IN_PROGRESS") TicketStatus status,
        @Schema(description = "Estimation in sprint units", example = "5") Integer estimation,
        @Schema(description = "Sprint ID") UUID sprintId
) {
}
