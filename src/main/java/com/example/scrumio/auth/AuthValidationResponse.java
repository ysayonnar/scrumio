package com.example.scrumio.auth;

import com.example.scrumio.entity.user.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record AuthValidationResponse(
        @JsonProperty("user_id")
        UUID userId,
        UserRole role
) { }
