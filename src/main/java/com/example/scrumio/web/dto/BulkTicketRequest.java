package com.example.scrumio.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request payload for bulk ticket creation during sprint planning")
public record BulkTicketRequest(
        @Schema(description = "Project ID all tickets belong to") @NotNull UUID projectId,
        @Schema(description = "Sprint ID to assign all tickets to (optional)") UUID sprintId,
        @Schema(description = "List of tickets to create") @NotNull @Size(min = 1) List<@Valid BulkTicketItemRequest> tickets
) {
}
