package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.project.ProjectMember;
import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.entity.sprint.SprintEstimationType;
import com.example.scrumio.entity.sprint.SprintStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private SprintMapper mapper;

    @InjectMocks
    private SprintService service;

    private UUID userId;
    private UUID projectId;
    private UUID sprintId;
    private Project project;
    private Sprint sprint;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        sprintId = UUID.randomUUID();
        project = new Project();
        project.setId(projectId);
        sprint = new Sprint();
        sprint.setId(sprintId);
        sprint.setProject(project);
    }

    private SprintResponse stubResponse(UUID id) {
        return new SprintResponse(id, "Sprint 1", null, null,
                LocalDate.now(), LocalDate.now().plusDays(14),
                SprintStatus.PLANNED, SprintEstimationType.STORY_POINTS, projectId, OffsetDateTime.now());
    }

    private void stubMembership() {
        ProjectMember pm = new ProjectMember();
        pm.setId(UUID.randomUUID());
        when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                .thenReturn(Optional.of(pm));
    }

    private SprintRequest validRequest() {
        return new SprintRequest("Sprint 1", null, null,
                LocalDate.now(), LocalDate.now().plusDays(14),
                SprintStatus.PLANNED, SprintEstimationType.STORY_POINTS, projectId);
    }

    @Nested
    class GetAll {

        @Test
        void shouldReturnSprints() {
            stubMembership();
            when(sprintRepository.findAllActiveByProjectId(projectId)).thenReturn(List.of(sprint));
            when(mapper.toResponse(sprint)).thenReturn(stubResponse(sprintId));

            List<SprintResponse> result = service.getAll(projectId, userId);

            assertEquals(1, result.size());
        }

        @Test
        void shouldThrowWhenNotMember() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.getAll(projectId, userId));
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnSprint() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.of(sprint));
            when(mapper.toResponse(sprint)).thenReturn(stubResponse(sprintId));

            SprintResponse result = service.getById(sprintId, userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.empty());

            assertThrows(SprintNotFoundException.class, () -> service.getById(sprintId, userId));
        }
    }

    @Nested
    class Create {

        @Test
        void shouldCreateSprint() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);
            when(mapper.toResponse(sprint)).thenReturn(stubResponse(sprintId));

            SprintResponse result = service.create(validRequest(), userId);

            assertNotNull(result);
            verify(sprintRepository).save(any(Sprint.class));
        }

        @Test
        void shouldThrowWhenStartDateNotBeforeEndDate() {
            stubMembership();
            SprintRequest request = new SprintRequest("Sprint 1", null, null,
                    LocalDate.now().plusDays(1), LocalDate.now(),
                    SprintStatus.PLANNED, SprintEstimationType.STORY_POINTS, projectId);

            assertThrows(IllegalArgumentException.class, () -> service.create(request, userId));
        }

        @Test
        void shouldThrowWhenNotMember() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());
            SprintRequest request = validRequest();

            assertThrows(ProjectNotFoundException.class, () -> service.create(request, userId));
        }

        @Test
        void shouldThrowWhenProjectNotFoundOnCreate() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.empty());

            SprintRequest request = validRequest();

            assertThrows(ProjectNotFoundException.class, () -> service.create(request, userId));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateSprint() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.of(sprint));
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);
            when(mapper.toResponse(sprint)).thenReturn(stubResponse(sprintId));

            SprintResponse result = service.update(sprintId, validRequest(), userId);

            assertNotNull(result);
            verify(sprintRepository).save(any(Sprint.class));
        }

        @Test
        void shouldThrowWhenSprintNotFoundOnUpdate() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.empty());

            SprintRequest request = validRequest();

            assertThrows(SprintNotFoundException.class, () -> service.update(sprintId, request, userId));
        }

        @Test
        void shouldThrowWhenProjectNotFoundOnUpdate() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.of(sprint));
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.empty());

            SprintRequest request = validRequest();

            assertThrows(ProjectNotFoundException.class, () -> service.update(sprintId, request, userId));
        }

        @Test
        void shouldThrowWhenStartDateNotBeforeEndDateOnUpdate() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.of(sprint));
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            SprintRequest request = new SprintRequest("Sprint 1", null, null,
                    LocalDate.now().plusDays(1), LocalDate.now(),
                    SprintStatus.PLANNED, SprintEstimationType.STORY_POINTS, projectId);

            assertThrows(IllegalArgumentException.class, () -> service.update(sprintId, request, userId));
        }
    }

    @Nested
    class Patch {

        @Test
        void shouldPatchOnlyProvidedFields() {
            sprint.setName("Original");
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.of(sprint));
            when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);
            when(mapper.toResponse(sprint)).thenReturn(stubResponse(sprintId));

            service.patch(sprintId, new SprintPatchRequest("Patched", null, null, null, null, null, null), userId);

            ArgumentCaptor<Sprint> captor = ArgumentCaptor.forClass(Sprint.class);
            verify(sprintRepository).save(captor.capture());
            assertEquals("Patched", captor.getValue().getName());
        }

        @Test
        void shouldPatchAllRemainingFields() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.of(sprint));
            when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);
            when(mapper.toResponse(sprint)).thenReturn(stubResponse(sprintId));

            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().plusDays(7);
            service.patch(sprintId, new SprintPatchRequest(null, "goal", "plan", start, end,
                    SprintStatus.ACTIVE, SprintEstimationType.HOURS), userId);

            ArgumentCaptor<Sprint> captor = ArgumentCaptor.forClass(Sprint.class);
            verify(sprintRepository).save(captor.capture());
            assertEquals("goal", captor.getValue().getBusinessGoal());
            assertEquals("plan", captor.getValue().getDevPlan());
            assertEquals(start, captor.getValue().getStartDate());
            assertEquals(end, captor.getValue().getEndDate());
            assertEquals(SprintStatus.ACTIVE, captor.getValue().getStatus());
            assertEquals(SprintEstimationType.HOURS, captor.getValue().getEstimationType());
        }

        @Test
        void shouldThrowWhenNotFoundOnPatch() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.empty());

            SprintPatchRequest request = new SprintPatchRequest(null, null, null, null, null, null, null);

            assertThrows(SprintNotFoundException.class, () -> service.patch(sprintId, request, userId));
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldSoftDeleteAndUnlinkTicketsAndMeetings() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.of(sprint));
            when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);
            when(mapper.toResponse(sprint)).thenReturn(stubResponse(sprintId));

            SprintResponse result = service.delete(sprintId, userId);

            assertNotNull(result);
            assertNotNull(sprint.getDeletedAt());
            verify(ticketRepository).unlinkFromSprint(sprintId);
            verify(meetingRepository).unlinkFromSprint(sprintId);
        }

        @Test
        void shouldThrowWhenNotFoundOnDelete() {
            when(sprintRepository.findActiveByIdForUser(sprintId, userId)).thenReturn(Optional.empty());

            assertThrows(SprintNotFoundException.class, () -> service.delete(sprintId, userId));
        }
    }
}
