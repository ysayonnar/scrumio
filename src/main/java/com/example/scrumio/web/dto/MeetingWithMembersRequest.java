package com.example.scrumio.web.dto;

import com.example.scrumio.entity.meeting.MeetingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MeetingWithMembersRequest(
        @NotBlank String title,
        String description,
        @NotNull MeetingType type,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt,
        UUID sprintId,
        @NotNull UUID projectId,
        @NotNull List<UUID> memberIds
) implements MeetingRequestData {
}
