package com.example.scrumio.entity.exception;

public class BadTicketPriorityException extends RuntimeException {
    public BadTicketPriorityException(String priority) {
        super("unknown priority: " + priority);
    }
}