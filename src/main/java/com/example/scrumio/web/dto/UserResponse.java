package com.example.scrumio.web.dto;

import com.example.scrumio.entity.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "User data returned from the API")
public record UserResponse(
        @Schema(description = "User ID") UUID id,
        @Schema(description = "Full name") String name,
        @Schema(description = "Email address") String email,
        @Schema(description = "User role") UserRole role,
        @Schema(description = "Creation timestamp") OffsetDateTime createdAt
) {
}
