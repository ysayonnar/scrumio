package com.example.scrumio.web.exception;

import java.util.UUID;

public class ProjectMemberNotFoundException extends NotFoundException {
    public ProjectMemberNotFoundException(UUID id) {
        super("ProjectMember not found: " + id);
    }
}
