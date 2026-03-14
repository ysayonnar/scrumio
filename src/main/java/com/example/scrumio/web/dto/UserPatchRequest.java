package com.example.scrumio.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for partially updating a user")
public record UserPatchRequest(
        @Schema(description = "Full name of the user", example = "Jane Doe") String name,
        @Schema(description = "Email address", example = "jane.doe@example.com") String email,
        @Schema(description = "Password", example = "newP@ssw0rd") String password
) {
}
