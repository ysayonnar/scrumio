package com.example.scrumio.web.dto;

import com.example.scrumio.entity.user.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        OffsetDateTime createdAt
) {
}
