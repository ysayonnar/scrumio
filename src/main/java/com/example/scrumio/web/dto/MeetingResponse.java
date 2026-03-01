package com.example.scrumio.web.dto;

import com.example.scrumio.entity.meeting.MeetingType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        String title,
        String description,
        MeetingType type,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        UUID sprintId,
        UUID projectId,
        OffsetDateTime createdAt
) {}
