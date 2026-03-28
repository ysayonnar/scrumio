package com.example.scrumio.concurrency;

import java.time.OffsetDateTime;

public class TaskState {

    private volatile TaskStatus status;
    private volatile String result;
    private final OffsetDateTime createdAt;

    public TaskState() {
        this.status = TaskStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
