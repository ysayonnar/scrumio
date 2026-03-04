package com.example.scrumio.web.dto;

import com.example.scrumio.entity.meeting.MeetingType;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface MeetingRequestData {
    String title();
    String description();
    MeetingType type();
    OffsetDateTime startsAt();
    OffsetDateTime endsAt();
    UUID sprintId();
    UUID projectId();
}
