package com.example.scrumio.web.dto;

import com.example.scrumio.entity.meeting.MeetingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request payload for creating a meeting with members in one call")
public record MeetingWithMembersRequest(
        @Schema(description = "Meeting title", example = "Sprint Review") @NotBlank String title,
        @Schema(description = "Meeting description") String description,
        @Schema(description = "Meeting type", example = "REVIEW") @NotNull MeetingType type,
        @Schema(description = "Meeting start time", example = "2026-03-15T14:00:00Z") @NotNull OffsetDateTime startsAt,
        @Schema(description = "Meeting end time", example = "2026-03-15T15:00:00Z") @NotNull OffsetDateTime endsAt,
        @Schema(description = "Sprint ID the meeting is associated with") UUID sprintId,
        @Schema(description = "Project ID the meeting belongs to") @NotNull UUID projectId,
        @Schema(description = "List of project member IDs to add to the meeting") @NotNull List<UUID> memberIds
) implements MeetingRequestData {
}
