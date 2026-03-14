package com.example.scrumio.web.dto;

import com.example.scrumio.entity.meeting.MeetingType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Meeting data returned from the API")
public record MeetingResponse(
        @Schema(description = "Meeting ID") UUID id,
        @Schema(description = "Meeting title") String title,
        @Schema(description = "Meeting description") String description,
        @Schema(description = "Meeting type") MeetingType type,
        @Schema(description = "Meeting start time") OffsetDateTime startsAt,
        @Schema(description = "Meeting end time") OffsetDateTime endsAt,
        @Schema(description = "Sprint ID") UUID sprintId,
        @Schema(description = "Project ID") UUID projectId,
        @Schema(description = "Creation timestamp") OffsetDateTime createdAt,
        @Schema(description = "List of meeting members") List<MeetingMemberResponse> members
) {
}
