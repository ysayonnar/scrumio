package com.example.scrumio.entity.exception;

import java.util.UUID;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(UUID id) {
        super("ticket with id " + id + " not found");
    }
}
