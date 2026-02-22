package com.example.scrumio.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {
    private UUID id;
    private String title;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
    private int estimation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private UUID sprintID;

    public Ticket() {}

    public Ticket(String title, String description, TicketPriority priority, TicketStatus status, int estimation, UUID sprintId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.estimation = estimation;
        this.sprintID = sprintId;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public int getEstimation() { return estimation; }
    public void setEstimation(int estimation) { this.estimation = estimation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public UUID getSprintID() { return sprintID; }
    public void setSprintID(UUID sprintID) { this.sprintID = sprintID; }
}