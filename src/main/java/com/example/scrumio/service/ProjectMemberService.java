package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.project.ProjectMember;
import com.example.scrumio.entity.project.ProjectMemberRole;
import com.example.scrumio.entity.user.User;
import com.example.scrumio.mapper.ProjectMemberMapper;
import com.example.scrumio.repository.MeetingMemberRepository;
import com.example.scrumio.repository.MemberTicketRepository;
import com.example.scrumio.repository.ProjectMemberRepository;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.UserRepository;
import com.example.scrumio.web.dto.ProjectMemberRequest;
import com.example.scrumio.web.dto.ProjectMemberResponse;
import com.example.scrumio.web.exception.ProjectMemberNotFoundException;
import com.example.scrumio.web.exception.ProjectNotFoundException;
import com.example.scrumio.web.exception.UnauthorizedException;
import com.example.scrumio.web.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MemberTicketRepository memberTicketRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final ProjectMemberMapper mapper;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository,
                                ProjectRepository projectRepository,
                                UserRepository userRepository,
                                MemberTicketRepository memberTicketRepository,
                                MeetingMemberRepository meetingMemberRepository,
                                ProjectMemberMapper mapper) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.memberTicketRepository = memberTicketRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getAll(UUID projectId, UUID userId) {
        verifyMembership(projectId, userId);
        return projectMemberRepository.findAllActiveByProjectId(projectId).stream()
                .map(mapper::toResponse).toList();
    }

    public ProjectMemberResponse addMember(UUID projectId, ProjectMemberRequest request, UUID userId) {
        ProjectMember requestingMember = projectMemberRepository.findActiveByProjectAndUser(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireOwnerOrManager(requestingMember);

        projectMemberRepository.findActiveByProjectAndUser(projectId, request.userId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("User is already a member of this project");
                });

        User user = userRepository.findActiveById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        Project project = projectRepository.findActiveById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        ProjectMember member = new ProjectMember();
        member.setUser(user);
        member.setProject(project);
        member.setRole(request.role());
        return mapper.toResponse(projectMemberRepository.save(member));
    }

    public ProjectMemberResponse updateRole(UUID projectId, UUID memberId, ProjectMemberRequest request, UUID userId) {
        ProjectMember requestingMember = projectMemberRepository.findActiveByProjectAndUser(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireOwnerOrManager(requestingMember);

        ProjectMember target = projectMemberRepository.findActiveById(memberId)
                .orElseThrow(() -> new ProjectMemberNotFoundException(memberId));
        target.setRole(request.role());
        return mapper.toResponse(projectMemberRepository.save(target));
    }

    @Transactional
    public ProjectMemberResponse removeMember(UUID projectId, UUID memberId, UUID userId) {
        ProjectMember requestingMember = projectMemberRepository.findActiveByProjectAndUser(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        if (requestingMember.getRole() != ProjectMemberRole.OWNER) {
            throw new UnauthorizedException("Only the project OWNER can remove members");
        }

        ProjectMember target = projectMemberRepository.findActiveById(memberId)
                .orElseThrow(() -> new ProjectMemberNotFoundException(memberId));
        if (target.getRole() == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove the project OWNER");
        }
        OffsetDateTime now = OffsetDateTime.now();
        memberTicketRepository.softDeleteAllActiveByMemberId(target.getId(), now);
        meetingMemberRepository.softDeleteAllActiveByMemberId(target.getId(), now);
        target.setDeletedAt(now);
        return mapper.toResponse(projectMemberRepository.save(target));
    }

    private void verifyMembership(UUID projectId, UUID userId) {
        projectMemberRepository.findActiveByProjectAndUser(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private void requireOwnerOrManager(ProjectMember member) {
        if (member.getRole() != ProjectMemberRole.OWNER && member.getRole() != ProjectMemberRole.MANAGER) {
            throw new UnauthorizedException("Only OWNER or MANAGER can perform this action");
        }
    }
}
