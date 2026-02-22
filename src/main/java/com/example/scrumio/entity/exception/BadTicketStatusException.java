package com.example.scrumio.entity.exception;

public class BadTicketStatusException extends RuntimeException {
    public BadTicketStatusException(String status) {
        super("unknown status: " + status);
    }
}
