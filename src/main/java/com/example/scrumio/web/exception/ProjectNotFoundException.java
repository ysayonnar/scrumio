package com.example.scrumio.web.exception;

import java.util.UUID;

public class ProjectNotFoundException extends NotFoundException {
    public ProjectNotFoundException(UUID id) {
        super("Project not found: " + id);
    }
}
