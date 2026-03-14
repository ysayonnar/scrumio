package com.example.scrumio.service;

import com.example.scrumio.cache.TicketCacheIndex;
import com.example.scrumio.cache.TicketCacheKey;
import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.entity.sprint.SprintStatus;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {

    private static final Logger LOG = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemberTicketRepository memberTicketRepository;
    private final TicketMapper mapper;
    private final TicketCacheIndex cacheIndex;

    public TicketService(TicketRepository ticketRepository,
                         ProjectRepository projectRepository,
                         SprintRepository sprintRepository,
                         ProjectMemberRepository projectMemberRepository,
                         MemberTicketRepository memberTicketRepository,
                         TicketMapper mapper,
                         TicketCacheIndex cacheIndex) {
        this.ticketRepository = ticketRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.memberTicketRepository = memberTicketRepository;
        this.mapper = mapper;
        this.cacheIndex = cacheIndex;
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAll(UUID projectId, UUID userId, TicketStatus status, TicketPriority priority,
                                       SprintStatus sprintStatus, Pageable pageable) {
        verifyMembership(projectId, userId);
        TicketCacheKey cacheKey = new TicketCacheKey(
                projectId,
                status != null ? status.name() : null,
                priority != null ? priority.name() : null,
                sprintStatus != null ? sprintStatus.name() : null,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        Optional<Page<TicketResponse>> cached = cacheIndex.get(cacheKey);
        if (cached.isPresent()) {
            LOG.info("[CACHE HIT]  key={}", cacheKey.hashCode());
            return cached.get();
        }
        LOG.info("[CACHE MISS] key={}", cacheKey.hashCode());
        Page<TicketResponse> result =
                ticketRepository.findAllActiveByProjectId(projectId, status, priority, sprintStatus, pageable)
                        .map(mapper::toResponse);
        cacheIndex.put(cacheKey, result);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllNative(UUID projectId, UUID userId, TicketStatus status, TicketPriority priority,
                                             SprintStatus sprintStatus, Pageable pageable) {
        verifyMembership(projectId, userId);
        String statusStr = status != null ? status.name() : null;
        String priorityStr = priority != null ? priority.name() : null;
        String sprintStatusStr = sprintStatus != null ? sprintStatus.name() : null;
        return ticketRepository.findAllActiveByProjectIdNative(projectId, statusStr, priorityStr, sprintStatusStr, pageable)
                .map(mapper::fromNativeProjection);
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
        Ticket ticket = new Ticket();
        return saveFromRequest(ticket, request);
    }

    public TicketResponse update(UUID id, TicketRequest request, UUID userId) {
        Ticket ticket = findActiveForUser(id, userId);
        return saveFromRequest(ticket, request);
    }

    private TicketResponse saveFromRequest(Ticket ticket, TicketRequest request) {
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
        TicketResponse response = mapper.toResponse(ticketRepository.save(ticket));
        cacheIndex.invalidateByProjectId(request.projectId());
        return response;
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
        UUID projectId = ticket.getProject().getId();
        TicketResponse response = mapper.toResponse(ticketRepository.save(ticket));
        cacheIndex.invalidateByProjectId(projectId);
        return response;
    }

    @Transactional
    public TicketResponse delete(UUID id, UUID userId) {
        Ticket ticket = findActiveForUser(id, userId);
        UUID projectId = ticket.getProject().getId();
        OffsetDateTime now = OffsetDateTime.now();
        memberTicketRepository.softDeleteAllActiveByTicketId(id, now);
        ticket.setDeletedAt(now);
        TicketResponse response = mapper.toResponse(ticketRepository.save(ticket));
        cacheIndex.invalidateByProjectId(projectId);
        return response;
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
