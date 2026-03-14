package com.example.scrumio.web.dto;

import com.example.scrumio.entity.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for creating or replacing a user")
public record UserRequest(
        @Schema(description = "Full name of the user", example = "Jane Doe") @NotBlank String name,
        @Schema(description = "Email address", example = "jane.doe@example.com") @NotBlank @Email String email,
        @Schema(description = "Password", example = "s3cur3P@ssw0rd") @NotBlank String password,
        @Schema(description = "User role", example = "MEMBER") @NotNull UserRole role
) {
}
