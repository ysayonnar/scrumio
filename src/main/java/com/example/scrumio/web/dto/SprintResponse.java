package com.example.scrumio.web.dto;

import com.example.scrumio.entity.sprint.SprintEstimationType;
import com.example.scrumio.entity.sprint.SprintStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SprintResponse(
        UUID id,
        String name,
        String businessGoal,
        String devPlan,
        LocalDate startDate,
        LocalDate endDate,
        SprintStatus status,
        SprintEstimationType estimationType,
        UUID projectId,
        OffsetDateTime createdAt
) {}
