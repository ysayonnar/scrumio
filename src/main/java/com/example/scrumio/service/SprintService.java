package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.mapper.SprintMapper;
import com.example.scrumio.repository.MeetingRepository;
import com.example.scrumio.repository.ProjectMemberRepository;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.SprintRepository;
import com.example.scrumio.repository.TicketRepository;
import com.example.scrumio.web.dto.SprintPatchRequest;
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
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TicketRepository ticketRepository;
    private final MeetingRepository meetingRepository;
    private final SprintMapper mapper;

    public SprintService(SprintRepository sprintRepository,
                         ProjectRepository projectRepository,
                         ProjectMemberRepository projectMemberRepository,
                         TicketRepository ticketRepository,
                         MeetingRepository meetingRepository,
                         SprintMapper mapper) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.ticketRepository = ticketRepository;
        this.meetingRepository = meetingRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> getAll(UUID projectId, UUID userId) {
        verifyMembership(projectId, userId);
        return sprintRepository.findAllActiveByProjectId(projectId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SprintResponse getById(UUID id, UUID userId) {
        return mapper.toResponse(findActiveForUser(id, userId));
    }

    public SprintResponse create(SprintRequest request, UUID userId) {
        verifyMembership(request.projectId(), userId);
        if (!request.startDate().isBefore(request.endDate())) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
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

    public SprintResponse update(UUID id, SprintRequest request, UUID userId) {
        Sprint sprint = findActiveForUser(id, userId);
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        if (!request.startDate().isBefore(request.endDate())) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
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

    public SprintResponse patch(UUID id, SprintPatchRequest request, UUID userId) {
        Sprint sprint = findActiveForUser(id, userId);
        if (request.name() != null) {
            sprint.setName(request.name());
        }
        if (request.businessGoal() != null) {
            sprint.setBusinessGoal(request.businessGoal());
        }
        if (request.devPlan() != null) {
            sprint.setDevPlan(request.devPlan());
        }
        if (request.startDate() != null) {
            sprint.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            sprint.setEndDate(request.endDate());
        }
        if (request.status() != null) {
            sprint.setStatus(request.status());
        }
        if (request.estimationType() != null) {
            sprint.setEstimationType(request.estimationType());
        }
        return mapper.toResponse(sprintRepository.save(sprint));
    }

    @Transactional
    public SprintResponse delete(UUID id, UUID userId) {
        Sprint sprint = findActiveForUser(id, userId);
        ticketRepository.unlinkFromSprint(id);
        meetingRepository.unlinkFromSprint(id);
        sprint.setDeletedAt(OffsetDateTime.now());
        return mapper.toResponse(sprintRepository.save(sprint));
    }

    private Sprint findActiveForUser(UUID id, UUID userId) {
        return sprintRepository.findActiveByIdForUser(id, userId)
                .orElseThrow(() -> new SprintNotFoundException(id));
    }

    private void verifyMembership(UUID projectId, UUID userId) {
        projectMemberRepository.findActiveByProjectAndUser(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
