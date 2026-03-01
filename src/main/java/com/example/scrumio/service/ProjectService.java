package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.user.User;
import com.example.scrumio.mapper.ProjectMapper;
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
    private final UserRepository userRepository;
    private final ProjectMapper mapper;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          ProjectMapper mapper) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAll() {
        return projectRepository.findAllActive().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(UUID id) {
        return mapper.toResponse(findActive(id));
    }

    public ProjectResponse create(ProjectRequest request) {
        User owner = userRepository.findActiveById(request.ownerId())
                .orElseThrow(() -> new UserNotFoundException(request.ownerId()));
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setOwner(owner);
        return mapper.toResponse(projectRepository.save(project));
    }

    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = findActive(id);
        User owner = userRepository.findActiveById(request.ownerId())
                .orElseThrow(() -> new UserNotFoundException(request.ownerId()));
        project.setName(request.name());
        project.setDescription(request.description());
        project.setOwner(owner);
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
