package com.example.scrumio.service;

import com.example.scrumio.entity.meeting.Meeting;
import com.example.scrumio.entity.meeting.MeetingType;
import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.project.ProjectMember;
import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.mapper.MeetingMapper;
import com.example.scrumio.repository.MeetingMemberRepository;
import com.example.scrumio.repository.MeetingRepository;
import com.example.scrumio.repository.ProjectMemberRepository;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.SprintRepository;
import com.example.scrumio.web.dto.MeetingPatchRequest;
import com.example.scrumio.web.dto.MeetingRequest;
import com.example.scrumio.web.dto.MeetingResponse;
import com.example.scrumio.web.dto.MeetingWithMembersRequest;
import com.example.scrumio.web.exception.MeetingNotFoundException;
import com.example.scrumio.web.exception.ProjectMemberNotFoundException;
import com.example.scrumio.web.exception.ProjectNotFoundException;
import com.example.scrumio.web.exception.SprintNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private SprintRepository sprintRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private MeetingMemberRepository meetingMemberRepository;
    @Mock
    private MeetingMapper mapper;

    @InjectMocks
    private MeetingService service;

    private UUID userId;
    private UUID projectId;
    private UUID sprintId;
    private UUID meetingId;
    private Project project;
    private Sprint sprint;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        sprintId = UUID.randomUUID();
        meetingId = UUID.randomUUID();
        project = new Project();
        project.setId(projectId);
        sprint = new Sprint();
        sprint.setId(sprintId);
        sprint.setProject(project);
    }

    private Meeting stubMeeting(UUID id) {
        Meeting meeting = new Meeting();
        meeting.setId(id);
        meeting.setProject(project);
        meeting.setTitle("Stand-up");
        return meeting;
    }

    private MeetingResponse stubResponse(UUID id) {
        return new MeetingResponse(id, "Stand-up", null, MeetingType.DAILY,
                OffsetDateTime.now(), OffsetDateTime.now().plusHours(1),
                null, projectId, OffsetDateTime.now(), Collections.emptyList());
    }

    private MeetingRequest validRequest() {
        OffsetDateTime start = OffsetDateTime.now();
        return new MeetingRequest("Stand-up", null, MeetingType.DAILY,
                start, start.plusHours(1), null, projectId);
    }

    private void stubMembership() {
        ProjectMember pm = new ProjectMember();
        pm.setId(UUID.randomUUID());
        when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId)).thenReturn(Optional.of(pm));
    }

    @Nested
    class GetAll {

        @Test
        void shouldReturnMeetings() {
            stubMembership();
            Meeting meeting = stubMeeting(meetingId);
            when(meetingRepository.findAllActiveByProjectId(projectId)).thenReturn(List.of(meeting));
            when(mapper.toResponse(meeting)).thenReturn(stubResponse(meetingId));

            List<MeetingResponse> result = service.getAll(projectId, userId);

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
    class Create {

        @Test
        void shouldCreateMeeting() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            Meeting saved = stubMeeting(meetingId);
            when(meetingRepository.save(any(Meeting.class))).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(stubResponse(meetingId));

            MeetingResponse result = service.create(validRequest(), userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenStartNotBeforeEnd() {
            stubMembership();
            OffsetDateTime time = OffsetDateTime.now();
            MeetingRequest request = new MeetingRequest("Stand-up", null, MeetingType.DAILY,
                    time, time, null, projectId);

            assertThrows(IllegalArgumentException.class, () -> service.create(request, userId));
        }

        @Test
        void shouldThrowWhenSprintNotFound() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(sprintRepository.findActiveById(sprintId)).thenReturn(Optional.empty());

            OffsetDateTime start = OffsetDateTime.now();
            MeetingRequest request = new MeetingRequest("Stand-up", null, MeetingType.DAILY,
                    start, start.plusHours(1), sprintId, projectId);

            assertThrows(SprintNotFoundException.class, () -> service.create(request, userId));
        }

        @Test
        void shouldThrowWhenSprintBelongsToDifferentProject() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            Project other = new Project();
            other.setId(UUID.randomUUID());
            Sprint otherSprint = new Sprint();
            otherSprint.setId(sprintId);
            otherSprint.setProject(other);
            when(sprintRepository.findActiveById(sprintId)).thenReturn(Optional.of(otherSprint));

            OffsetDateTime start = OffsetDateTime.now();
            MeetingRequest request = new MeetingRequest("Stand-up", null, MeetingType.DAILY,
                    start, start.plusHours(1), sprintId, projectId);

            assertThrows(IllegalArgumentException.class, () -> service.create(request, userId));
        }

        @Test
        void shouldCreateMeetingWithValidSprint() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(sprintRepository.findActiveById(sprintId)).thenReturn(Optional.of(sprint));
            Meeting saved = stubMeeting(meetingId);
            when(meetingRepository.save(any(Meeting.class))).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(stubResponse(meetingId));

            OffsetDateTime start = OffsetDateTime.now();
            MeetingRequest request = new MeetingRequest("Stand-up", null, MeetingType.DAILY,
                    start, start.plusHours(1), sprintId, projectId);
            MeetingResponse result = service.create(request, userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenNotMemberOnCreate() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.create(validRequest(), userId));
        }

        @Test
        void shouldThrowWhenProjectNotFoundOnCreate() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.create(validRequest(), userId));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateMeeting() {
            Meeting meeting = stubMeeting(meetingId);
            when(meetingRepository.findActiveByIdForUser(meetingId, userId)).thenReturn(Optional.of(meeting));
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);
            when(mapper.toResponse(meeting)).thenReturn(stubResponse(meetingId));

            MeetingResponse result = service.update(meetingId, validRequest(), userId);

            assertNotNull(result);
            verify(meetingRepository).save(any(Meeting.class));
        }

        @Test
        void shouldThrowWhenMeetingNotFoundOnUpdate() {
            when(meetingRepository.findActiveByIdForUser(meetingId, userId)).thenReturn(Optional.empty());

            assertThrows(MeetingNotFoundException.class, () -> service.update(meetingId, validRequest(), userId));
        }

        @Test
        void shouldThrowWhenInvalidTimesOnUpdate() {
            Meeting meeting = stubMeeting(meetingId);
            when(meetingRepository.findActiveByIdForUser(meetingId, userId)).thenReturn(Optional.of(meeting));
            OffsetDateTime time = OffsetDateTime.now();
            MeetingRequest request = new MeetingRequest("Stand-up", null, MeetingType.DAILY,
                    time, time, null, projectId);

            assertThrows(IllegalArgumentException.class, () -> service.update(meetingId, request, userId));
        }
    }

    @Nested
    class Patch {

        @Test
        void shouldPatchAllProvidedFields() {
            Meeting meeting = stubMeeting(meetingId);
            when(meetingRepository.findActiveByIdForUser(meetingId, userId)).thenReturn(Optional.of(meeting));
            when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);
            when(mapper.toResponse(meeting)).thenReturn(stubResponse(meetingId));

            OffsetDateTime start = OffsetDateTime.now();
            MeetingPatchRequest request = new MeetingPatchRequest("Updated", "desc", MeetingType.REVIEW,
                    start, start.plusHours(2));
            MeetingResponse result = service.patch(meetingId, request, userId);

            assertNotNull(result);
            verify(meetingRepository).save(any(Meeting.class));
        }

        @Test
        void shouldPatchWithNullFields() {
            Meeting meeting = stubMeeting(meetingId);
            when(meetingRepository.findActiveByIdForUser(meetingId, userId)).thenReturn(Optional.of(meeting));
            when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);
            when(mapper.toResponse(meeting)).thenReturn(stubResponse(meetingId));

            MeetingPatchRequest request = new MeetingPatchRequest(null, null, null, null, null);
            MeetingResponse result = service.patch(meetingId, request, userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenNotFoundOnPatch() {
            when(meetingRepository.findActiveByIdForUser(meetingId, userId)).thenReturn(Optional.empty());

            assertThrows(MeetingNotFoundException.class,
                    () -> service.patch(meetingId, new MeetingPatchRequest(null, null, null, null, null), userId));
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnMeeting() {
            Meeting meeting = stubMeeting(meetingId);
            when(meetingRepository.findActiveByIdForUser(meetingId, userId)).thenReturn(Optional.of(meeting));
            when(mapper.toResponse(meeting)).thenReturn(stubResponse(meetingId));

            MeetingResponse result = service.getById(meetingId, userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(meetingRepository.findActiveByIdForUser(meetingId, userId)).thenReturn(Optional.empty());

            assertThrows(MeetingNotFoundException.class, () -> service.getById(meetingId, userId));
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldSoftDeleteMeetingAndMembers() {
            Meeting meeting = stubMeeting(meetingId);
            when(meetingRepository.findActiveByIdForUser(meetingId, userId)).thenReturn(Optional.of(meeting));
            when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);
            when(mapper.toResponse(meeting)).thenReturn(stubResponse(meetingId));

            MeetingResponse result = service.delete(meetingId, userId);

            assertNotNull(result);
            assertNotNull(meeting.getDeletedAt());
            verify(meetingMemberRepository).softDeleteAllActiveByMeetingId(eq(meetingId), any(OffsetDateTime.class));
        }
    }

    @Nested
    class CreateWithMembers {

        @Test
        void shouldCreateMeetingWithAllMembers() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));

            UUID member1 = UUID.randomUUID();
            UUID member2 = UUID.randomUUID();
            ProjectMember pm1 = new ProjectMember();
            pm1.setId(member1);
            ProjectMember pm2 = new ProjectMember();
            pm2.setId(member2);
            when(projectMemberRepository.findAllActiveByIdsAndProjectId(any(), eq(projectId)))
                    .thenReturn(List.of(pm1, pm2));

            OffsetDateTime start = OffsetDateTime.now();
            MeetingWithMembersRequest request = new MeetingWithMembersRequest(
                    "Review", null, MeetingType.REVIEW,
                    start, start.plusHours(1), null, projectId, List.of(member1, member2));
            when(mapper.toResponse(any(Meeting.class))).thenReturn(stubResponse(meetingId));

            MeetingResponse result = service.createWithMembers(request, userId);

            assertNotNull(result);
            verify(meetingMemberRepository, times(2)).save(any());
        }

        @Test
        void shouldThrowWhenMemberNotFound() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));

            UUID memberId = UUID.randomUUID();
            when(projectMemberRepository.findAllActiveByIdsAndProjectId(any(), eq(projectId)))
                    .thenReturn(Collections.emptyList());

            OffsetDateTime start = OffsetDateTime.now();
            MeetingWithMembersRequest request = new MeetingWithMembersRequest(
                    "Review", null, MeetingType.REVIEW,
                    start, start.plusHours(1), null, projectId, List.of(memberId));

            assertThrows(ProjectMemberNotFoundException.class,
                    () -> service.createWithMembers(request, userId));
        }
    }

    @Nested
    class CreateWithMembersUnsafe {

        @Test
        void shouldCreateMeetingWithMembersUnsafe() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));

            UUID member1 = UUID.randomUUID();
            UUID member2 = UUID.randomUUID();
            ProjectMember pm1 = new ProjectMember();
            pm1.setId(member1);
            ProjectMember pm2 = new ProjectMember();
            pm2.setId(member2);
            when(projectMemberRepository.findActiveByIdAndProjectId(member1, projectId))
                    .thenReturn(Optional.of(pm1));
            when(projectMemberRepository.findActiveByIdAndProjectId(member2, projectId))
                    .thenReturn(Optional.of(pm2));

            OffsetDateTime start = OffsetDateTime.now();
            MeetingWithMembersRequest request = new MeetingWithMembersRequest(
                    "Review", null, MeetingType.REVIEW,
                    start, start.plusHours(1), null, projectId, List.of(member1, member2));
            when(mapper.toResponse(any(Meeting.class))).thenReturn(stubResponse(meetingId));

            MeetingResponse result = service.createWithMembersUnsafe(request, userId);

            assertNotNull(result);
            verify(meetingMemberRepository, times(2)).save(any());
            verify(projectMemberRepository, never()).findAllActiveByIdsAndProjectId(any(), any());
        }

        @Test
        void shouldThrowWhenMemberNotFoundInUnsafe() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));

            UUID invalidMemberId = UUID.randomUUID();
            when(projectMemberRepository.findActiveByIdAndProjectId(invalidMemberId, projectId))
                    .thenReturn(Optional.empty());

            OffsetDateTime start = OffsetDateTime.now();
            MeetingWithMembersRequest request = new MeetingWithMembersRequest(
                    "Review", null, MeetingType.REVIEW,
                    start, start.plusHours(1), null, projectId, List.of(invalidMemberId));

            assertThrows(ProjectMemberNotFoundException.class,
                    () -> service.createWithMembersUnsafe(request, userId));
            verify(projectMemberRepository, never()).findAllActiveByIdsAndProjectId(any(), any());
        }
    }
}
