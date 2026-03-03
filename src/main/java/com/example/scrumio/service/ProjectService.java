package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.project.ProjectMember;
import com.example.scrumio.entity.project.ProjectMemberRole;
import com.example.scrumio.entity.user.User;
import com.example.scrumio.mapper.ProjectMapper;
import com.example.scrumio.repository.ProjectMemberRepository;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.UserRepository;
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
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper mapper;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository,
                          ProjectMapper mapper) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
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

    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = findActive(id);
        project.setName(request.name());
        project.setDescription(request.description());
        return mapper.toResponse(projectRepository.save(project));
    }

    public ProjectResponse delete(UUID id) {
        Project project = findActive(id);
        project.setDeletedAt(OffsetDateTime.now());
        return mapper.toResponse(projectRepository.save(project));
    }

    private Project findActive(UUID id) {
        return projectRepository.findActiveById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }
}
