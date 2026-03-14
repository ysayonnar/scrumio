package com.example.scrumio.web.dto;

import com.example.scrumio.entity.project.ProjectMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Ticket assignment data returned from the API")
public record MemberTicketResponse(
        @Schema(description = "Assignment ID") UUID id,
        @Schema(description = "Project member ID") UUID memberId,
        @Schema(description = "User ID") UUID userId,
        @Schema(description = "User name") String userName,
        @Schema(description = "Member role in the project") ProjectMemberRole role,
        @Schema(description = "Ticket ID") UUID ticketId,
        @Schema(description = "Creation timestamp") OffsetDateTime createdAt
) {}
