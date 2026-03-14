package com.example.scrumio.web.dto;

import com.example.scrumio.entity.project.ProjectMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Meeting member data returned from the API")
public record MeetingMemberResponse(
        @Schema(description = "Project member ID") UUID projectMemberId,
        @Schema(description = "User ID") UUID userId,
        @Schema(description = "User name") String userName,
        @Schema(description = "User email") String userEmail,
        @Schema(description = "Member role in the project") ProjectMemberRole role
) {
}
