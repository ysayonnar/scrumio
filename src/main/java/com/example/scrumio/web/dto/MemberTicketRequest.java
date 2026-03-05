package com.example.scrumio.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MemberTicketRequest(@NotNull UUID memberId) {}
