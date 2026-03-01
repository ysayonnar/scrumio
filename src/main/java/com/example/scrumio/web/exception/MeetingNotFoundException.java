package com.example.scrumio.web.exception;

import java.util.UUID;

public class MeetingNotFoundException extends NotFoundException {
    public MeetingNotFoundException(UUID id) {
        super("Meeting not found: " + id);
    }
}
