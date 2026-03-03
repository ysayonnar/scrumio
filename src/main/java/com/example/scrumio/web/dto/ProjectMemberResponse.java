package com.example.scrumio.web.dto;

import com.example.scrumio.entity.project.ProjectMemberRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UUID userId,
        String userName,
        UUID projectId,
        ProjectMemberRole role,
        OffsetDateTime createdAt
) {
}
