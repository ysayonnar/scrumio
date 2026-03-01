package com.example.scrumio.web.dto;

import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TicketRequest(
        @NotBlank String title,
        String description,
        TicketPriority priority,
        @NotNull TicketStatus status,
        Integer estimation,
        UUID sprintId,
        @NotNull UUID projectId
) {}
