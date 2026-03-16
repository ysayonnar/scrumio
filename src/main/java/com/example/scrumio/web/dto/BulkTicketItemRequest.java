package com.example.scrumio.web.dto;

import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Single ticket item within a bulk creation request")
public record BulkTicketItemRequest(
        @Schema(description = "Ticket title", example = "Implement login page") @NotBlank String title,
        @Schema(description = "Ticket description") String description,
        @Schema(description = "Ticket priority", example = "HIGH") TicketPriority priority,
        @Schema(description = "Ticket status", example = "TODO") @NotNull TicketStatus status,
        @Schema(description = "Estimation in sprint units", example = "3") Integer estimation,
        @Schema(description = "Project member IDs to assign to this ticket") List<UUID> memberIds
) {
}
