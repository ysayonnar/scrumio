package com.example.scrumio.web.dto;

import com.example.scrumio.entity.sprint.SprintEstimationType;
import com.example.scrumio.entity.sprint.SprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Sprint data returned from the API")
public record SprintResponse(
        @Schema(description = "Sprint ID") UUID id,
        @Schema(description = "Sprint name") String name,
        @Schema(description = "Business goal") String businessGoal,
        @Schema(description = "Development plan") String devPlan,
        @Schema(description = "Start date") LocalDate startDate,
        @Schema(description = "End date") LocalDate endDate,
        @Schema(description = "Sprint status") SprintStatus status,
        @Schema(description = "Estimation type") SprintEstimationType estimationType,
        @Schema(description = "Project ID") UUID projectId,
        @Schema(description = "Creation timestamp") OffsetDateTime createdAt
) {
}
