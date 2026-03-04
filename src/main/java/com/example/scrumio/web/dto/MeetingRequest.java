package com.example.scrumio.web.dto;

import com.example.scrumio.entity.meeting.MeetingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingRequest(
        @NotBlank String title,
        String description,
        @NotNull MeetingType type,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt,
        UUID sprintId,
        @NotNull UUID projectId
) implements MeetingRequestData {
}
