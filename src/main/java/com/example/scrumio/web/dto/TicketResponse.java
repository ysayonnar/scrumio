package com.example.scrumio.web.dto;

import com.example.scrumio.entity.TicketPriority;
import com.example.scrumio.entity.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        int estimation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        UUID sprintID
){}
