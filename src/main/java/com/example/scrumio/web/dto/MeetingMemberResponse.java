package com.example.scrumio.web.dto;

import com.example.scrumio.entity.project.ProjectMemberRole;

import java.util.UUID;

public record MeetingMemberResponse(
        UUID projectMemberId,
        UUID userId,
        String userName,
        String userEmail,
        ProjectMemberRole role
) {
}
