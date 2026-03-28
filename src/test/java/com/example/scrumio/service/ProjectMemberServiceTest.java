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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MemberTicketRepository memberTicketRepository;
    @Mock
    private MeetingMemberRepository meetingMemberRepository;
    @Mock
    private ProjectMemberMapper mapper;

    @InjectMocks
    private ProjectMemberService service;

    private UUID userId;
    private UUID projectId;
    private UUID memberId;
    private ProjectMember requestingMember;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        requestingMember = new ProjectMember();
        requestingMember.setId(UUID.randomUUID());
        requestingMember.setRole(ProjectMemberRole.OWNER);
    }

    private ProjectMemberResponse stubResponse(UUID id) {
        return new ProjectMemberResponse(id, userId, "User", projectId, ProjectMemberRole.DEVELOPER, OffsetDateTime.now());
    }

    @Nested
    class GetAll {

        @Test
        void shouldReturnMembers() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));
            ProjectMember pm = new ProjectMember();
            pm.setId(memberId);
            when(projectMemberRepository.findAllActiveByProjectId(projectId)).thenReturn(List.of(pm));
            when(mapper.toResponse(pm)).thenReturn(stubResponse(memberId));

            List<ProjectMemberResponse> result = service.getAll(projectId, userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenNotMember() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.getAll(projectId, userId));
        }
    }

    @Nested
    class AddMember {

        @Test
        void shouldAddMemberWhenManager() {
            requestingMember.setRole(ProjectMemberRole.MANAGER);
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));

            UUID newUserId = UUID.randomUUID();
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, newUserId))
                    .thenReturn(Optional.empty());

            User user = new User();
            user.setId(newUserId);
            when(userRepository.findActiveById(newUserId)).thenReturn(Optional.of(user));

            Project project = new Project();
            project.setId(projectId);
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));

            ProjectMember saved = new ProjectMember();
            saved.setId(memberId);
            when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(stubResponse(memberId));

            ProjectMemberRequest request = new ProjectMemberRequest(newUserId, ProjectMemberRole.DEVELOPER);
            ProjectMemberResponse result = service.addMember(projectId, request, userId);

            assertNotNull(result);
        }

        @Test
        void shouldAddMemberWhenOwner() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));

            UUID newUserId = UUID.randomUUID();
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, newUserId))
                    .thenReturn(Optional.empty());

            User user = new User();
            user.setId(newUserId);
            when(userRepository.findActiveById(newUserId)).thenReturn(Optional.of(user));

            Project project = new Project();
            project.setId(projectId);
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));

            ProjectMember saved = new ProjectMember();
            saved.setId(memberId);
            when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(stubResponse(memberId));

            ProjectMemberRequest request = new ProjectMemberRequest(newUserId, ProjectMemberRole.DEVELOPER);
            ProjectMemberResponse result = service.addMember(projectId, request, userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenNotOwnerOrManager() {
            requestingMember.setRole(ProjectMemberRole.DEVELOPER);
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));

            UUID newUserId = UUID.randomUUID();
            ProjectMemberRequest request = new ProjectMemberRequest(newUserId, ProjectMemberRole.DEVELOPER);
            assertThrows(UnauthorizedException.class, () -> service.addMember(projectId, request, userId));
        }

        @Test
        void shouldThrowWhenUserAlreadyMember() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));

            UUID newUserId = UUID.randomUUID();
            ProjectMember existing = new ProjectMember();
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, newUserId))
                    .thenReturn(Optional.of(existing));

            ProjectMemberRequest request = new ProjectMemberRequest(newUserId, ProjectMemberRole.DEVELOPER);
            assertThrows(IllegalArgumentException.class, () -> service.addMember(projectId, request, userId));
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));

            UUID newUserId = UUID.randomUUID();
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, newUserId))
                    .thenReturn(Optional.empty());
            when(userRepository.findActiveById(newUserId)).thenReturn(Optional.empty());

            ProjectMemberRequest request = new ProjectMemberRequest(newUserId, ProjectMemberRole.DEVELOPER);
            assertThrows(UserNotFoundException.class, () -> service.addMember(projectId, request, userId));
        }
    }

    @Nested
    class UpdateRole {

        @Test
        void shouldUpdateRole() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));
            ProjectMember target = new ProjectMember();
            target.setId(memberId);
            target.setRole(ProjectMemberRole.DEVELOPER);
            when(projectMemberRepository.findActiveById(memberId)).thenReturn(Optional.of(target));
            when(projectMemberRepository.save(target)).thenReturn(target);
            when(mapper.toResponse(target)).thenReturn(stubResponse(memberId));

            ProjectMemberRequest request = new ProjectMemberRequest(UUID.randomUUID(), ProjectMemberRole.MANAGER);
            ProjectMemberResponse result = service.updateRole(projectId, memberId, request, userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenNotOwnerOrManagerOnUpdateRole() {
            requestingMember.setRole(ProjectMemberRole.DEVELOPER);
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));

            ProjectMemberRequest request = new ProjectMemberRequest(UUID.randomUUID(), ProjectMemberRole.MANAGER);
            assertThrows(UnauthorizedException.class,
                    () -> service.updateRole(projectId, memberId, request, userId));
        }

        @Test
        void shouldThrowWhenTargetMemberNotFound() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));
            when(projectMemberRepository.findActiveById(memberId)).thenReturn(Optional.empty());

            ProjectMemberRequest request = new ProjectMemberRequest(UUID.randomUUID(), ProjectMemberRole.MANAGER);
            assertThrows(ProjectMemberNotFoundException.class,
                    () -> service.updateRole(projectId, memberId, request, userId));
        }
    }

    @Nested
    class RemoveMember {

        @Test
        void shouldRemoveMemberAndCascadeDelete() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));

            ProjectMember target = new ProjectMember();
            target.setId(memberId);
            target.setRole(ProjectMemberRole.DEVELOPER);
            when(projectMemberRepository.findActiveById(memberId)).thenReturn(Optional.of(target));
            when(projectMemberRepository.save(target)).thenReturn(target);
            when(mapper.toResponse(target)).thenReturn(stubResponse(memberId));

            ProjectMemberResponse result = service.removeMember(projectId, memberId, userId);

            assertNotNull(result);
            assertNotNull(target.getDeletedAt());
            verify(memberTicketRepository).softDeleteAllActiveByMemberId(eq(memberId), any(OffsetDateTime.class));
            verify(meetingMemberRepository).softDeleteAllActiveByMemberId(eq(memberId), any(OffsetDateTime.class));
        }

        @Test
        void shouldThrowWhenRemoverIsNotOwner() {
            requestingMember.setRole(ProjectMemberRole.MANAGER);
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));

            assertThrows(UnauthorizedException.class, () -> service.removeMember(projectId, memberId, userId));
        }

        @Test
        void shouldThrowWhenRemovingOwner() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.of(requestingMember));

            ProjectMember owner = new ProjectMember();
            owner.setId(memberId);
            owner.setRole(ProjectMemberRole.OWNER);
            when(projectMemberRepository.findActiveById(memberId)).thenReturn(Optional.of(owner));

            assertThrows(IllegalArgumentException.class, () -> service.removeMember(projectId, memberId, userId));
        }
    }
}
