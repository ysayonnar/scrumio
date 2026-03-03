package com.example.scrumio.web.dto;

import com.example.scrumio.entity.project.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectMemberRequest(
        @NotNull UUID userId,
        @NotNull ProjectMemberRole role
) {
}
