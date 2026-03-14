package com.example.scrumio.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Project data returned from the API")
public record ProjectResponse(
        @Schema(description = "Project ID") UUID id,
        @Schema(description = "Project name") String name,
        @Schema(description = "Project description") String description,
        @Schema(description = "ID of the project owner") UUID ownerId,
        @Schema(description = "Creation timestamp") OffsetDateTime createdAt,
        @Schema(description = "Last update timestamp") OffsetDateTime updatedAt,
        @Schema(description = "Deletion timestamp, null if active") OffsetDateTime deletedAt
) {
}
