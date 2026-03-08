package com.example.scrumio.cache;

import java.util.Objects;
import java.util.UUID;

public final class TicketCacheKey {

    private final UUID projectId;
    private final String status;
    private final String priority;
    private final String sprintStatus;
    private final int page;
    private final int size;

    public TicketCacheKey(UUID projectId, String status, String priority, String sprintStatus, int page, int size) {
        this.projectId = projectId;
        this.status = status;
        this.priority = priority;
        this.sprintStatus = sprintStatus;
        this.page = page;
        this.size = size;
    }

    public UUID projectId() {
        return projectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TicketCacheKey other)) {
            return false;
        }

        return page == other.page
                && size == other.size
                && Objects.equals(projectId, other.projectId)
                && Objects.equals(status, other.status)
                && Objects.equals(priority, other.priority)
                && Objects.equals(sprintStatus, other.sprintStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, status, priority, sprintStatus, page, size);
    }

    @Override
    public String toString() {
        return "project=" + projectId + " status=" + status + " priority=" + priority
                + " sprintStatus=" + sprintStatus + " page=" + page + " size=" + size;
    }
}
