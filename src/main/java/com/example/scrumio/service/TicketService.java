package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.entity.ticket.Ticket;
import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import com.example.scrumio.mapper.TicketMapper;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.SprintRepository;
import com.example.scrumio.repository.TicketRepository;
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
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final TicketMapper mapper;

    public TicketService(TicketRepository ticketRepository,
                         ProjectRepository projectRepository,
                         SprintRepository sprintRepository,
                         TicketMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAll(String statusStr, String priorityStr) {
        TicketStatus status = statusStr != null
                ? TicketStatus.valueOf(statusStr.toUpperCase()) : null;
        TicketPriority priority = priorityStr != null
                ? TicketPriority.valueOf(priorityStr.toUpperCase()) : null;
        return ticketRepository.findAllActive(status, priority).stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getByID(UUID id) {
        return mapper.toResponse(findActive(id));
    }

    public TicketResponse create(TicketRequest request) {
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId());
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

    public TicketResponse update(UUID id, TicketRequest request) {
        Ticket ticket = findActive(id);
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId());
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setStatus(request.status());
        ticket.setEstimation(request.estimation());
        ticket.setSprint(sprint);
        ticket.setProject(project);
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    public TicketResponse delete(UUID id) {
        Ticket ticket = findActive(id);
        ticket.setDeletedAt(OffsetDateTime.now());
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    private Ticket findActive(UUID id) {
        return ticketRepository.findActiveById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    private Sprint resolveSprint(UUID sprintId) {
        if (sprintId == null) return null;
        return sprintRepository.findActiveById(sprintId)
                .orElseThrow(() -> new SprintNotFoundException(sprintId));
    }
}
