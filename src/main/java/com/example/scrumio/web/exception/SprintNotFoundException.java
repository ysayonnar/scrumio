package com.example.scrumio.web.exception;

import java.util.UUID;

public class SprintNotFoundException extends NotFoundException {
    public SprintNotFoundException(UUID id) {
        super("Sprint not found: " + id);
    }
}
