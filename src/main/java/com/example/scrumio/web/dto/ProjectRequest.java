package com.example.scrumio.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectRequest(
        @NotBlank String name,
        String description,
        @NotNull UUID ownerId
) {
}
