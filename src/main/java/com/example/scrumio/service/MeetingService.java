package com.example.scrumio.service;

import com.example.scrumio.entity.meeting.Meeting;
import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.mapper.MeetingMapper;
import com.example.scrumio.repository.MeetingRepository;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.SprintRepository;
import com.example.scrumio.web.dto.MeetingRequest;
import com.example.scrumio.web.dto.MeetingResponse;
import com.example.scrumio.web.exception.MeetingNotFoundException;
import com.example.scrumio.web.exception.ProjectNotFoundException;
import com.example.scrumio.web.exception.SprintNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final MeetingMapper mapper;

    public MeetingService(MeetingRepository meetingRepository,
                          ProjectRepository projectRepository,
                          SprintRepository sprintRepository,
                          MeetingMapper mapper) {
        this.meetingRepository = meetingRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MeetingResponse> getAll() {
        return meetingRepository.findAllActive().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MeetingResponse getById(UUID id) {
        return mapper.toResponse(findActive(id));
    }

    public MeetingResponse create(MeetingRequest request) {
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId());
        Meeting meeting = new Meeting();
        meeting.setTitle(request.title());
        meeting.setDescription(request.description());
        meeting.setType(request.type());
        meeting.setStartsAt(request.startsAt());
        meeting.setEndsAt(request.endsAt());
        meeting.setSprint(sprint);
        meeting.setProject(project);
        return mapper.toResponse(meetingRepository.save(meeting));
    }

    public MeetingResponse update(UUID id, MeetingRequest request) {
        Meeting meeting = findActive(id);
        Project project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId());
        meeting.setTitle(request.title());
        meeting.setDescription(request.description());
        meeting.setType(request.type());
        meeting.setStartsAt(request.startsAt());
        meeting.setEndsAt(request.endsAt());
        meeting.setSprint(sprint);
        meeting.setProject(project);
        return mapper.toResponse(meetingRepository.save(meeting));
    }

    public MeetingResponse delete(UUID id) {
        Meeting meeting = findActive(id);
        meeting.setDeletedAt(OffsetDateTime.now());
        return mapper.toResponse(meetingRepository.save(meeting));
    }

    private Meeting findActive(UUID id) {
        return meetingRepository.findActiveById(id)
                .orElseThrow(() -> new MeetingNotFoundException(id));
    }

    private Sprint resolveSprint(UUID sprintId) {
        if (sprintId == null) {
            return null;
        }
        return sprintRepository.findActiveById(sprintId)
                .orElseThrow(() -> new SprintNotFoundException(sprintId));
    }
}
