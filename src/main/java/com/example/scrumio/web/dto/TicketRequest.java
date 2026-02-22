package com.example.scrumio.web.dto;


import com.example.scrumio.entity.TicketPriority;
import com.example.scrumio.entity.TicketStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record TicketRequest(
        @NotBlank(message = "title can't be blank")
        String title,

        @NotBlank(message = "description can't be blank")
        String description,

        @NotNull(message = "invalid priority")
        TicketPriority priority,

        @NotNull(message = "invalid status")
        TicketStatus status,

        @Positive(message = "estimation can't be negative")
        int estimation,

        @JsonProperty("sprint_id")
        @NotNull(message = "sprint_id can't be null")
        UUID sprintID
) {
}
