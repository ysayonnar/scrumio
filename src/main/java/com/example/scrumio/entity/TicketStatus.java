package com.example.scrumio.entity;

import java.util.Arrays;
import java.util.Optional;

public enum TicketStatus{
    BACKLOG,
    TODO,
    IN_PROGRESS,
    ON_HOLD,
    ON_REVIEW,
    DONE;

    public static Optional<TicketStatus> from(String value) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
