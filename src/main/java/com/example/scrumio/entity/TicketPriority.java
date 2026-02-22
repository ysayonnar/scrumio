package com.example.scrumio.entity;

import java.util.Arrays;
import java.util.Optional;

public enum TicketPriority {
    LOW,
    MEDIUM,
    HIGH;

    public static Optional<TicketPriority> from(String value) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
