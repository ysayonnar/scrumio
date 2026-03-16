package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.project.ProjectMember;
import com.example.scrumio.entity.project.ProjectMemberRole;
import com.example.scrumio.entity.user.User;
import com.example.scrumio.mapper.ProjectMapper;
import com.example.scrumio.repository.MeetingMemberRepository;
import com.example.scrumio.repository.MeetingRepository;
import com.example.scrumio.repository.MemberTicketRepository;
import com.example.scrumio.repository.ProjectMemberRepository;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.SprintRepository;
import com.example.scrumio.repository.TicketRepository;
import com.example.scrumio.repository.UserRepository;
import com.example.scrumio.web.dto.ProjectPatchRequest;
import com.example.scrumio.web.dto.ProjectRequest;
import com.example.scrumio.web.dto.ProjectResponse;
import com.example.scrumio.web.exception.ProjectNotFoundException;
import com.example.scrumio.web.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SprintRepository sprintRepository;
    private final TicketRepository ticketRepository;
    private final MeetingRepository meetingRepository;
    private final MemberTicketRepository memberTicketRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper mapper;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          SprintRepository sprintRepository,
                          TicketRepository ticketRepository,
                          MeetingRepository meetingRepository,
                          MemberTicketRepository memberTicketRepository,
                          MeetingMemberRepository meetingMemberRepository,
                          UserRepository userRepository,
                          ProjectMapper mapper) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.sprintRepository = sprintRepository;
        this.ticketRepository = ticketRepository;
        this.meetingRepository = meetingRepository;
        this.memberTicketRepository = memberTicketRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAll(UUID userId) {
        return projectRepository.findAllActiveUserProjects(userId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(UUID id, UUID userId) {
        return mapper.toResponse(
                projectRepository.findActiveByIdForUser(id, userId)
                        .orElseThrow(() -> new ProjectNotFoundException(id))
        );
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request, UUID userId) {
        User owner = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setOwner(owner);
        Project createdProject = projectRepository.save(project);

        ProjectMember member = new ProjectMember();
        member.setUser(owner);
        member.setProject(project);
        member.setRole(ProjectMemberRole.OWNER);
        projectMemberRepository.save(member);

        return mapper.toResponse(createdProject);
    }

    public ProjectResponse update(UUID id, ProjectRequest request, UUID userId) {
        Project project = projectRepository.findActiveByIdForUser(id, userId)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        project.setName(request.name());
        project.setDescription(request.description());
        return mapper.toResponse(projectRepository.save(project));
    }

    public ProjectResponse patch(UUID id, ProjectPatchRequest request, UUID userId) {
        Project project = projectRepository.findActiveByIdForUser(id, userId)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        if (request.name() != null) {
            project.setName(request.name());
        }

        if (request.description() != null) {
            project.setDescription(request.description());
        }
        return mapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse delete(UUID id, UUID userId) {
        Project project = projectRepository.findActiveByIdForUser(id, userId)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        cascadeDelete(project);
        return mapper.toResponse(project);
    }

    @Transactional
    public void cascadeDeleteAllByOwner(UUID ownerId) {
        OffsetDateTime now = OffsetDateTime.now();
        projectRepository.findAllActiveByOwnerId(ownerId)
                .forEach(project -> cascadeDeleteRelations(project.getId(), now));
        projectRepository.softDeleteAllActiveByOwnerId(ownerId, now);
    }

    private void cascadeDelete(Project project) {
        OffsetDateTime now = OffsetDateTime.now();
        cascadeDeleteRelations(project.getId(), now);
        project.setDeletedAt(now);
        projectRepository.save(project);
    }

    private void cascadeDeleteRelations(UUID projectId, OffsetDateTime now) {
        memberTicketRepository.softDeleteAllActiveByProjectId(projectId, now);
        meetingMemberRepository.softDeleteAllActiveByProjectId(projectId, now);
        ticketRepository.softDeleteAllActiveByProjectId(projectId, now);
        meetingRepository.softDeleteAllActiveByProjectId(projectId, now);
        sprintRepository.softDeleteAllActiveByProjectId(projectId, now);
        projectMemberRepository.softDeleteAllActiveByProjectId(projectId, now);
    }
}
