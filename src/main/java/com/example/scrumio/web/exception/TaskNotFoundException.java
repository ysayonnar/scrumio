package com.example.scrumio.web.exception;

import java.util.UUID;

public class TaskNotFoundException extends NotFoundException {
    public TaskNotFoundException(UUID id) {
        super("Task not found: " + id);
    }
}
