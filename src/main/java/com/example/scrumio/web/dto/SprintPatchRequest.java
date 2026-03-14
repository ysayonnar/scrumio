package com.example.scrumio.web.dto;

import com.example.scrumio.entity.sprint.SprintEstimationType;
import com.example.scrumio.entity.sprint.SprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Request payload for partially updating a sprint")
public record SprintPatchRequest(
        @Schema(description = "Sprint name", example = "Sprint 2") String name,
        @Schema(description = "Business goal") String businessGoal,
        @Schema(description = "Development plan") String devPlan,
        @Schema(description = "Start date", example = "2026-03-15") LocalDate startDate,
        @Schema(description = "End date", example = "2026-03-28") LocalDate endDate,
        @Schema(description = "Sprint status", example = "ACTIVE") SprintStatus status,
        @Schema(description = "Estimation type", example = "HOURS") SprintEstimationType estimationType
) {
}
