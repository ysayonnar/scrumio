package com.example.scrumio.web.dto;

import com.example.scrumio.entity.sprint.SprintEstimationType;
import com.example.scrumio.entity.sprint.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record SprintRequest(
        @NotBlank String name,
        String businessGoal,
        String devPlan,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull SprintStatus status,
        @NotNull SprintEstimationType estimationType,
        @NotNull UUID projectId
) {}
