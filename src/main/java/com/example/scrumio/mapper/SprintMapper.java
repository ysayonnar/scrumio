package com.example.scrumio.mapper;

import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.web.dto.SprintResponse;
import org.springframework.stereotype.Component;

@Component
public class SprintMapper {

    public SprintResponse toResponse(Sprint sprint) {
        return new SprintResponse(
                sprint.getId(),
                sprint.getName(),
                sprint.getBusinessGoal(),
                sprint.getDevPlan(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getStatus(),
                sprint.getEstimationType(),
                sprint.getProject().getId(),
                sprint.getCreatedAt()
        );
    }
}
