package com.example.scrumio.web.dto;

import com.example.scrumio.entity.project.ProjectMemberRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MemberTicketResponse(
        UUID id,
        UUID memberId,
        UUID userId,
        String userName,
        ProjectMemberRole role,
        UUID ticketId,
        OffsetDateTime createdAt
) {}
