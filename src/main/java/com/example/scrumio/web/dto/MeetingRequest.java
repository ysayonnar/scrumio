package com.example.scrumio.web.dto;

import com.example.scrumio.entity.meeting.MeetingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Request payload for creating or replacing a meeting")
public record MeetingRequest(
        @Schema(description = "Meeting title", example = "Sprint Planning") @NotBlank String title,
        @Schema(description = "Meeting description") String description,
        @Schema(description = "Meeting type", example = "PLANNING") @NotNull MeetingType type,
        @Schema(description = "Meeting start time", example = "2026-03-15T10:00:00Z") @NotNull OffsetDateTime startsAt,
        @Schema(description = "Meeting end time", example = "2026-03-15T11:00:00Z") @NotNull OffsetDateTime endsAt,
        @Schema(description = "Sprint ID the meeting is associated with") UUID sprintId,
        @Schema(description = "Project ID the meeting belongs to") @NotNull UUID projectId
) implements MeetingRequestData {
}
