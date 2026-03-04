package com.example.scrumio.web.dto;

import com.example.scrumio.entity.sprint.SprintEstimationType;
import com.example.scrumio.entity.sprint.SprintStatus;

import java.time.LocalDate;

public record SprintPatchRequest(
        String name,
        String businessGoal,
        String devPlan,
        LocalDate startDate,
        LocalDate endDate,
        SprintStatus status,
        SprintEstimationType estimationType
) {
}
