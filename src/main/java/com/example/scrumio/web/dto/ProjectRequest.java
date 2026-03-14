package com.example.scrumio.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for creating or replacing a project")
public record ProjectRequest(
        @Schema(description = "Project name", example = "Scrumio Backend") @NotBlank String name,
        @Schema(description = "Project description", example = "REST API for project management") String description
) {
}
