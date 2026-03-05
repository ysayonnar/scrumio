package com.example.scrumio.web.exception;

import java.util.UUID;

public class MemberTicketNotFoundException extends NotFoundException {
    public MemberTicketNotFoundException(UUID id) {
        super("Assignment not found: " + id);
    }
}
