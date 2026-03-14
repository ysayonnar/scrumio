package com.example.scrumio.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload for assigning a member to a ticket")
public record MemberTicketRequest(
        @Schema(description = "Project member ID to assign") @NotNull UUID memberId
) {}
