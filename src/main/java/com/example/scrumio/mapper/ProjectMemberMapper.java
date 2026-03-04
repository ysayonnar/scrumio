package com.example.scrumio.mapper;

import com.example.scrumio.entity.project.ProjectMember;
import com.example.scrumio.web.dto.ProjectMemberResponse;
import org.springframework.stereotype.Component;

@Component
public class ProjectMemberMapper {

    public ProjectMemberResponse toResponse(ProjectMember pm) {
        return new ProjectMemberResponse(
                pm.getId(),
                pm.getUser().getId(),
                pm.getUser().getName(),
                pm.getProject().getId(),
                pm.getRole(),
                pm.getCreatedAt()
        );
    }
}
