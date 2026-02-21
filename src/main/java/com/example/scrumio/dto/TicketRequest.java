package com.example.scrumio.dto;

import com.example.scrumio.entity.TicketPriority;
import com.example.scrumio.entity.TicketStatus;

import java.util.UUID;

public record TicketRequest(
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        int estimation,
        UUID sprintID
){}
