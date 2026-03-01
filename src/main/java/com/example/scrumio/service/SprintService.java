package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.mapper.SprintMapper;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.SprintRepository;
import com.example.scrumio.web.dto.SprintRequest;
import com.example.scrumio.web.dto.SprintResponse;
import com.example.scrumio.web.exception.ProjectNotFoundException;
import com.example.scrumio.web.exception.SprintNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final SprintMapper mapper;

    public SprintService(SprintRepository sprintRepository,
                         ProjectRepository projectRepository,
                         SprintMapper mapper) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> getAll() {
        return sprintRepository.findAllActive().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SprintResponse getById(UUID id) {
        return mapper.toResponse(findActive(id));
    }

    public SprintResponse create(SprintRequest request) {
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = new Sprint();
        sprint.setName(request.name());
        sprint.setBusinessGoal(request.businessGoal());
        sprint.setDevPlan(request.devPlan());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setStatus(request.status());
        sprint.setEstimationType(request.estimationType());
        sprint.setProject(project);
        return mapper.toResponse(sprintRepository.save(sprint));
    }

    public SprintResponse update(UUID id, SprintRequest request) {
        Sprint sprint = findActive(id);
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        sprint.setName(request.name());
        sprint.setBusinessGoal(request.businessGoal());
        sprint.setDevPlan(request.devPlan());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setStatus(request.status());
        sprint.setEstimationType(request.estimationType());
        sprint.setProject(project);
        return mapper.toResponse(sprintRepository.save(sprint));
    }

    public SprintResponse delete(UUID id) {
        Sprint sprint = findActive(id);
        sprint.setDeletedAt(OffsetDateTime.now());
        return mapper.toResponse(sprintRepository.save(sprint));
    }

    private Sprint findActive(UUID id) {
        return sprintRepository.findActiveById(id)
                .orElseThrow(() -> new SprintNotFoundException(id));
    }
}
