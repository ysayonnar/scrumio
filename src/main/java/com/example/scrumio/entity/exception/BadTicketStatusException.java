package com.example.scrumio.entity.exception;

import java.util.UUID;

public class BadTicketStatusException extends RuntimeException {
    public BadTicketStatusException(String status) {
        super("unknown status: " + status);
    }
}
