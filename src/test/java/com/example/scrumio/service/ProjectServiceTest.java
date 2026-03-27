package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private SprintRepository sprintRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private MemberTicketRepository memberTicketRepository;
    @Mock
    private MeetingMemberRepository meetingMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectMapper mapper;

    @InjectMocks
    private ProjectService service;

    private UUID userId;
    private UUID projectId;
    private User owner;
    private Project project;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        owner = new User();
        owner.setId(userId);
        project = new Project();
        project.setId(projectId);
        project.setName("Test Project");
        project.setOwner(owner);
    }

    private ProjectResponse stubResponse(UUID id) {
        return new ProjectResponse(id, "Test Project", "desc", userId, OffsetDateTime.now(), OffsetDateTime.now(), null);
    }

    @Nested
    class GetAll {

        @Test
        void shouldReturnUserProjects() {
            ProjectResponse response = stubResponse(projectId);
            when(projectRepository.findAllActiveUserProjects(userId)).thenReturn(List.of(project));
            when(mapper.toResponse(project)).thenReturn(response);

            List<ProjectResponse> result = service.getAll(userId);

            assertEquals(1, result.size());
            assertEquals(response, result.get(0));
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnProject() {
            ProjectResponse response = stubResponse(projectId);
            when(projectRepository.findActiveByIdForUser(projectId, userId)).thenReturn(Optional.of(project));
            when(mapper.toResponse(project)).thenReturn(response);

            ProjectResponse result = service.getById(projectId, userId);

            assertEquals(response, result);
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(projectRepository.findActiveByIdForUser(projectId, userId)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.getById(projectId, userId));
        }
    }

    @Nested
    class Create {

        @Test
        void shouldCreateProjectAndAddOwnerAsMember() {
            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(owner));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(mapper.toResponse(project)).thenReturn(stubResponse(projectId));

            ProjectRequest request = new ProjectRequest("Test Project", "desc");
            ProjectResponse result = service.create(request, userId);

            assertNotNull(result);
            verify(projectMemberRepository).save(any());
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());

            ProjectRequest request = new ProjectRequest("Test Project", "desc");
            assertThrows(UserNotFoundException.class, () -> service.create(request, userId));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateProject() {
            when(projectRepository.findActiveByIdForUser(projectId, userId)).thenReturn(Optional.of(project));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(mapper.toResponse(project)).thenReturn(stubResponse(projectId));

            ProjectRequest request = new ProjectRequest("Updated", "new desc");
            ProjectResponse result = service.update(projectId, request, userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenNotFoundOnUpdate() {
            when(projectRepository.findActiveByIdForUser(projectId, userId)).thenReturn(Optional.empty());
            ProjectRequest request = new ProjectRequest("Updated", null);

            assertThrows(ProjectNotFoundException.class, () -> service.update(projectId, request, userId));
        }
    }

    @Nested
    class Patch {

        @Test
        void shouldPatchOnlyProvidedFields() {
            project.setName("Original");
            project.setDescription("Original desc");
            when(projectRepository.findActiveByIdForUser(projectId, userId)).thenReturn(Optional.of(project));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(mapper.toResponse(project)).thenReturn(stubResponse(projectId));

            service.patch(projectId, new ProjectPatchRequest("Patched", null), userId);

            ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
            verify(projectRepository).save(captor.capture());
            assertEquals("Patched", captor.getValue().getName());
            assertEquals("Original desc", captor.getValue().getDescription());
        }

        @Test
        void shouldPatchDescriptionOnly() {
            project.setName("Original");
            project.setDescription("Original desc");
            when(projectRepository.findActiveByIdForUser(projectId, userId)).thenReturn(Optional.of(project));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(mapper.toResponse(project)).thenReturn(stubResponse(projectId));

            service.patch(projectId, new ProjectPatchRequest(null, "New desc"), userId);

            ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
            verify(projectRepository).save(captor.capture());
            assertEquals("Original", captor.getValue().getName());
            assertEquals("New desc", captor.getValue().getDescription());
        }

        @Test
        void shouldThrowWhenNotFoundOnPatch() {
            when(projectRepository.findActiveByIdForUser(projectId, userId)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.patch(projectId, new ProjectPatchRequest("x", null), userId));
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldCascadeDeleteAllRelated() {
            when(projectRepository.findActiveByIdForUser(projectId, userId)).thenReturn(Optional.of(project));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(mapper.toResponse(project)).thenReturn(stubResponse(projectId));

            ProjectResponse result = service.delete(projectId, userId);

            assertNotNull(result);
            assertNotNull(project.getDeletedAt());
            verify(memberTicketRepository).softDeleteAllActiveByProjectId(eq(projectId), any(OffsetDateTime.class));
            verify(ticketRepository).softDeleteAllActiveByProjectId(eq(projectId), any(OffsetDateTime.class));
            verify(sprintRepository).softDeleteAllActiveByProjectId(eq(projectId), any(OffsetDateTime.class));
            verify(meetingRepository).softDeleteAllActiveByProjectId(eq(projectId), any(OffsetDateTime.class));
            verify(projectMemberRepository).softDeleteAllActiveByProjectId(eq(projectId), any(OffsetDateTime.class));
        }
    }

    @Nested
    class CascadeDeleteByOwner {

        @Test
        void shouldDeleteAllOwnerProjects() {
            when(projectRepository.findAllActiveByOwnerId(userId)).thenReturn(List.of(project));

            service.cascadeDeleteAllByOwner(userId);

            verify(memberTicketRepository).softDeleteAllActiveByProjectId(eq(projectId), any(OffsetDateTime.class));
            verify(projectRepository).softDeleteAllActiveByOwnerId(eq(userId), any(OffsetDateTime.class));
        }
    }
}
