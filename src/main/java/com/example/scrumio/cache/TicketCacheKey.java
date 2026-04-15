package com.example.scrumio.cache;

import java.util.Objects;
import java.util.UUID;

public final class TicketCacheKey {

    private final UUID projectId;
    private final String status;
    private final String priority;
    private final String sprintStatus;
    private final UUID sprintId;
    private final int page;
    private final int size;

    public TicketCacheKey(UUID projectId, String status, String priority, String sprintStatus, UUID sprintId, int page, int size) {
        this.projectId = projectId;
        this.status = status;
        this.priority = priority;
        this.sprintStatus = sprintStatus;
        this.sprintId = sprintId;
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
                && Objects.equals(sprintStatus, other.sprintStatus)
                && Objects.equals(sprintId, other.sprintId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, status, priority, sprintStatus, sprintId, page, size);
    }

    @Override
    public String toString() {
        return "project=" + projectId + " status=" + status + " priority=" + priority
                + " sprintStatus=" + sprintStatus + " sprintId=" + sprintId + " page=" + page + " size=" + size;
    }
}
