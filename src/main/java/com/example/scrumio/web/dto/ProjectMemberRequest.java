package com.example.scrumio.web.dto;

import com.example.scrumio.entity.project.ProjectMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload for adding or updating a project member")
public record ProjectMemberRequest(
        @Schema(description = "ID of the user to add") @NotNull UUID userId,
        @Schema(description = "Role to assign", example = "DEVELOPER") @NotNull ProjectMemberRole role
) {
}
