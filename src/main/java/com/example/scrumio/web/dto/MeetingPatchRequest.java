package com.example.scrumio.web.dto;

import com.example.scrumio.entity.meeting.MeetingType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Request payload for partially updating a meeting")
public record MeetingPatchRequest(
        @Schema(description = "Meeting title", example = "Daily Standup") String title,
        @Schema(description = "Meeting description") String description,
        @Schema(description = "Meeting type", example = "DAILY") MeetingType type,
        @Schema(description = "Meeting start time", example = "2026-03-15T09:00:00Z") OffsetDateTime startsAt,
        @Schema(description = "Meeting end time", example = "2026-03-15T09:15:00Z") OffsetDateTime endsAt
) {
}
