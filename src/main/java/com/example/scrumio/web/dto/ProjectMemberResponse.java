package com.example.scrumio.web.dto;

import com.example.scrumio.entity.project.ProjectMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Project member data returned from the API")
public record ProjectMemberResponse(
        @Schema(description = "Project member ID") UUID id,
        @Schema(description = "User ID") UUID userId,
        @Schema(description = "User name") String userName,
        @Schema(description = "Project ID") UUID projectId,
        @Schema(description = "Member role in the project") ProjectMemberRole role,
        @Schema(description = "Creation timestamp") OffsetDateTime createdAt
) {
}
