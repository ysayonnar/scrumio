package com.example.scrumio.web.dto;

import com.example.scrumio.entity.sprint.SprintEstimationType;
import com.example.scrumio.entity.sprint.SprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request payload for creating or replacing a sprint")
public record SprintRequest(
        @Schema(description = "Sprint name", example = "Sprint 1") @NotBlank String name,
        @Schema(description = "Business goal for the sprint") String businessGoal,
        @Schema(description = "Development plan for the sprint") String devPlan,
        @Schema(description = "Sprint start date", example = "2026-03-01") @NotNull LocalDate startDate,
        @Schema(description = "Sprint end date", example = "2026-03-14") @NotNull LocalDate endDate,
        @Schema(description = "Sprint status", example = "PLANNED") @NotNull SprintStatus status,
        @Schema(description = "Estimation type", example = "STORY_POINTS") @NotNull SprintEstimationType estimationType,
        @Schema(description = "Project ID the sprint belongs to") @NotNull UUID projectId
) {
}
