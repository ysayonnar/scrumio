package com.example.scrumio.web.dto;

import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Ticket data returned from the API")
public record TicketResponse(
        @Schema(description = "Ticket ID") UUID id,
        @Schema(description = "Ticket title") String title,
        @Schema(description = "Ticket description") String description,
        @Schema(description = "Ticket priority") TicketPriority priority,
        @Schema(description = "Ticket status") TicketStatus status,
        @Schema(description = "Estimation in sprint units") Integer estimation,
        @Schema(description = "Sprint ID") UUID sprintId,
        @Schema(description = "Sprint name") String sprintName,
        @Schema(description = "Project ID") UUID projectId,
        @Schema(description = "Creation timestamp") OffsetDateTime createdAt
) {
}
