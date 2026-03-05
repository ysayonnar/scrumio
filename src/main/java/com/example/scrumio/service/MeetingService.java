package com.example.scrumio.service;

import com.example.scrumio.entity.meeting.Meeting;
import com.example.scrumio.entity.meeting.MeetingMember;
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
import com.example.scrumio.web.dto.MeetingRequestData;
import com.example.scrumio.web.dto.MeetingResponse;
import com.example.scrumio.web.dto.MeetingWithMembersRequest;
import com.example.scrumio.web.exception.MeetingNotFoundException;
import com.example.scrumio.web.exception.ProjectMemberNotFoundException;
import com.example.scrumio.web.exception.ProjectNotFoundException;
import com.example.scrumio.web.exception.SprintNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingMapper mapper;

    public MeetingService(MeetingRepository meetingRepository,
                          ProjectRepository projectRepository,
                          SprintRepository sprintRepository,
                          ProjectMemberRepository projectMemberRepository,
                          MeetingMemberRepository meetingMemberRepository,
                          MeetingMapper mapper) {
        this.meetingRepository = meetingRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MeetingResponse> getAll(UUID projectId, UUID userId) {
        verifyMembership(projectId, userId);
        return meetingRepository.findAllActiveByProjectId(projectId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MeetingResponse getById(UUID id, UUID userId) {
        return mapper.toResponse(findActiveForUser(id, userId));
    }

    public MeetingResponse create(MeetingRequest request, UUID userId) {
        verifyMembership(request.projectId(), userId);
        validateTimes(request);
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId(), request.projectId());
        Meeting meeting = new Meeting();
        applyFields(meeting, request, sprint, project);
        return mapper.toResponse(meetingRepository.save(meeting));
    }

    public MeetingResponse update(UUID id, MeetingRequest request, UUID userId) {
        Meeting meeting = findActiveForUser(id, userId);
        validateTimes(request);
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId(), request.projectId());
        applyFields(meeting, request, sprint, project);
        return mapper.toResponse(meetingRepository.save(meeting));
    }

    public MeetingResponse patch(UUID id, MeetingPatchRequest request, UUID userId) {
        Meeting meeting = findActiveForUser(id, userId);
        if (request.title() != null) {
            meeting.setTitle(request.title());
        }
        if (request.description() != null) {
            meeting.setDescription(request.description());
        }
        if (request.type() != null) {
            meeting.setType(request.type());
        }
        if (request.startsAt() != null) {
            meeting.setStartsAt(request.startsAt());
        }
        if (request.endsAt() != null) {
            meeting.setEndsAt(request.endsAt());
        }
        return mapper.toResponse(meetingRepository.save(meeting));
    }

    @Transactional
    public MeetingResponse delete(UUID id, UUID userId) {
        Meeting meeting = findActiveForUser(id, userId);
        OffsetDateTime now = OffsetDateTime.now();
        meetingMemberRepository.softDeleteAllActiveByMeetingId(id, now);
        meeting.setDeletedAt(now);
        return mapper.toResponse(meetingRepository.save(meeting));
    }

    @Transactional
    public MeetingResponse createWithMembers(MeetingWithMembersRequest request, UUID userId) {
        Meeting meeting = buildAndSaveMeeting(request, userId);
        List<ProjectMember> members = projectMemberRepository
                .findAllActiveByIdsAndProjectId(request.memberIds(), request.projectId());
        if (members.size() != request.memberIds().size()) {
            throw new ProjectMemberNotFoundException(null);
        }
        for (ProjectMember pm : members) {
            addMemberToMeeting(meeting, pm);
        }
        return mapper.toResponse(meeting);
    }

    public MeetingResponse createWithMembersUnsafe(MeetingWithMembersRequest request, UUID userId) {
        Meeting meeting = buildAndSaveMeeting(request, userId);
        for (UUID memberId : request.memberIds()) {
            ProjectMember pm = projectMemberRepository.findActiveByIdAndProjectId(memberId, request.projectId())
                    .orElseThrow(() -> new ProjectMemberNotFoundException(memberId));
            addMemberToMeeting(meeting, pm);
        }
        return mapper.toResponse(meeting);
    }

    private Meeting buildAndSaveMeeting(MeetingWithMembersRequest request, UUID userId) {
        verifyMembership(request.projectId(), userId);
        validateTimes(request);
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId(), request.projectId());
        Meeting meeting = new Meeting();
        applyFields(meeting, request, sprint, project);
        meetingRepository.save(meeting);
        return meeting;
    }

    private void validateTimes(MeetingRequestData request) {
        if (!request.startsAt().isBefore(request.endsAt())) {
            throw new IllegalArgumentException("startsAt must be before endsAt");
        }
    }

    private void applyFields(Meeting meeting, MeetingRequestData request, Sprint sprint, Project project) {
        meeting.setTitle(request.title());
        meeting.setDescription(request.description());
        meeting.setType(request.type());
        meeting.setStartsAt(request.startsAt());
        meeting.setEndsAt(request.endsAt());
        meeting.setSprint(sprint);
        meeting.setProject(project);
    }

    private void addMemberToMeeting(Meeting meeting, ProjectMember pm) {
        MeetingMember mm = new MeetingMember();
        mm.setMeeting(meeting);
        mm.setMember(pm);
        meetingMemberRepository.save(mm);
        meeting.getMembers().add(mm);
    }

    private Meeting findActiveForUser(UUID id, UUID userId) {
        return meetingRepository.findActiveByIdForUser(id, userId)
                .orElseThrow(() -> new MeetingNotFoundException(id));
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
