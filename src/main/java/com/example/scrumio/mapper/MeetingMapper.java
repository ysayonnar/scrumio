package com.example.scrumio.mapper;

import com.example.scrumio.entity.meeting.Meeting;
import com.example.scrumio.web.dto.MeetingResponse;
import org.springframework.stereotype.Component;

@Component
public class MeetingMapper {

    public MeetingResponse toResponse(Meeting meeting) {
        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getType(),
                meeting.getStartsAt(),
                meeting.getEndsAt(),
                meeting.getSprint() != null ? meeting.getSprint().getId() : null,
                meeting.getProject().getId(),
                meeting.getCreatedAt()
        );
    }
}
