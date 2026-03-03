package com.example.scrumio.web.dto;

import com.example.scrumio.entity.meeting.MeetingType;

import java.time.OffsetDateTime;

public record MeetingPatchRequest(
        String title,
        String description,
        MeetingType type,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt
) {
}
