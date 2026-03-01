package com.example.scrumio.web.exception;

import java.util.UUID;

public class TicketNotFoundException extends NotFoundException {
    public TicketNotFoundException(UUID id) {
        super("Ticket not found: " + id);
    }
}
