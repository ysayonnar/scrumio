package com.example.scrumio.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for partially updating a project")
public record ProjectPatchRequest(
        @Schema(description = "Project name", example = "Scrumio Backend") String name,
        @Schema(description = "Project description", example = "Updated description") String description
) {
}
