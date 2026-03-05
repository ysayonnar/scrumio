package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.entity.ticket.Ticket;
import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import com.example.scrumio.mapper.TicketMapper;
import com.example.scrumio.repository.MemberTicketRepository;
import com.example.scrumio.repository.ProjectMemberRepository;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.SprintRepository;
import com.example.scrumio.repository.TicketRepository;
import com.example.scrumio.web.dto.TicketPatchRequest;
import com.example.scrumio.web.dto.TicketRequest;
import com.example.scrumio.web.dto.TicketResponse;
import com.example.scrumio.web.exception.ProjectNotFoundException;
import com.example.scrumio.web.exception.SprintNotFoundException;
import com.example.scrumio.web.exception.TicketNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemberTicketRepository memberTicketRepository;
    private final TicketMapper mapper;

    public TicketService(TicketRepository ticketRepository,
                         ProjectRepository projectRepository,
                         SprintRepository sprintRepository,
                         ProjectMemberRepository projectMemberRepository,
                         MemberTicketRepository memberTicketRepository,
                         TicketMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.memberTicketRepository = memberTicketRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAll(UUID projectId, UUID userId, String statusStr, String priorityStr) {
        verifyMembership(projectId, userId);
        TicketStatus status = statusStr != null ? TicketStatus.valueOf(statusStr.toUpperCase()) : null;
        TicketPriority priority = priorityStr != null ? TicketPriority.valueOf(priorityStr.toUpperCase()) : null;
        return ticketRepository.findAllActiveByProjectId(projectId, status, priority).stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllSafe(UUID projectId, UUID userId) {
        verifyMembership(projectId, userId);
        return ticketRepository.findAllActiveByProjectIdSafe(projectId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllUnsafe(UUID projectId, UUID userId) {
        verifyMembership(projectId, userId);
        return ticketRepository.findAllActiveByProjectIdUnsafe(projectId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getByID(UUID id, UUID userId) {
        return mapper.toResponse(findActiveForUser(id, userId));
    }

    public TicketResponse create(TicketRequest request, UUID userId) {
        verifyMembership(request.projectId(), userId);
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId(), request.projectId());
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setStatus(request.status());
        ticket.setEstimation(request.estimation());
        ticket.setSprint(sprint);
        ticket.setProject(project);
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    public TicketResponse update(UUID id, TicketRequest request, UUID userId) {
        Ticket ticket = findActiveForUser(id, userId);
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId(), request.projectId());
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setStatus(request.status());
        ticket.setEstimation(request.estimation());
        ticket.setSprint(sprint);
        ticket.setProject(project);
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    public TicketResponse patch(UUID id, TicketPatchRequest request, UUID userId) {
        Ticket ticket = findActiveForUser(id, userId);

        if (request.title() != null) {
            ticket.setTitle(request.title());
        }
        if (request.description() != null) {
            ticket.setDescription(request.description());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (request.status() != null) {
            ticket.setStatus(request.status());
        }
        if (request.estimation() != null) {
            ticket.setEstimation(request.estimation());
        }
        if (request.sprintId() != null) {
            Sprint sprint = resolveSprint(request.sprintId(), ticket.getProject().getId());
            ticket.setSprint(sprint);
        }
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse delete(UUID id, UUID userId) {
        Ticket ticket = findActiveForUser(id, userId);
        OffsetDateTime now = OffsetDateTime.now();
        memberTicketRepository.softDeleteAllActiveByTicketId(id, now);
        ticket.setDeletedAt(now);
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    private Ticket findActiveForUser(UUID id, UUID userId) {
        return ticketRepository.findActiveByIdForUser(id, userId)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    private void verifyMembership(UUID projectId, UUID userId) {
        projectMemberRepository.findActiveByProjectAndUser(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private Sprint resolveSprint(UUID sprintId, UUID projectId) {
        if (sprintId == null) {
            return null;
        }
        Sprint sprint = sprintRepository.findActiveById(sprintId)
                .orElseThrow(() -> new SprintNotFoundException(sprintId));
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Sprint does not belong to the specified project");
        }
        return sprint;
    }
}
