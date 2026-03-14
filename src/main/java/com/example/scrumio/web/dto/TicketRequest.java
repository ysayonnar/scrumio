package com.example.scrumio.web.dto;

import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload for creating or replacing a ticket")
public record TicketRequest(
        @Schema(description = "Ticket title", example = "Fix login bug") @NotBlank String title,
        @Schema(description = "Ticket description") String description,
        @Schema(description = "Ticket priority", example = "HIGH") TicketPriority priority,
        @Schema(description = "Ticket status", example = "TODO") @NotNull TicketStatus status,
        @Schema(description = "Estimation in sprint units", example = "3") Integer estimation,
        @Schema(description = "Sprint ID the ticket belongs to") UUID sprintId,
        @Schema(description = "Project ID the ticket belongs to") @NotNull UUID projectId
) {
}
