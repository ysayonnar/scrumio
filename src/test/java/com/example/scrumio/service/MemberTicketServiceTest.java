package com.example.scrumio.service;

import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.project.ProjectMember;
import com.example.scrumio.entity.project.ProjectMemberRole;
import com.example.scrumio.entity.ticket.MemberTicket;
import com.example.scrumio.entity.ticket.Ticket;
import com.example.scrumio.mapper.MemberTicketMapper;
import com.example.scrumio.repository.MemberTicketRepository;
import com.example.scrumio.repository.ProjectMemberRepository;
import com.example.scrumio.repository.TicketRepository;
import com.example.scrumio.web.dto.MemberTicketRequest;
import com.example.scrumio.web.dto.MemberTicketResponse;
import com.example.scrumio.web.exception.MemberTicketNotFoundException;
import com.example.scrumio.web.exception.ProjectMemberNotFoundException;
import com.example.scrumio.web.exception.ProjectNotFoundException;
import com.example.scrumio.web.exception.TicketNotFoundException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberTicketServiceTest {

    @Mock
    private MemberTicketRepository memberTicketRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private MemberTicketMapper mapper;

    @InjectMocks
    private MemberTicketService service;

    private UUID userId;
    private UUID projectId;
    private UUID ticketId;
    private UUID memberId;
    private Project project;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        project = new Project();
        project.setId(projectId);
        ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setProject(project);
    }

    private MemberTicketResponse stubResponse(UUID id) {
        return new MemberTicketResponse(id, memberId, userId, "User", ProjectMemberRole.DEVELOPER, ticketId, OffsetDateTime.now());
    }

    private void stubTicket() {
        when(ticketRepository.findActiveById(ticketId)).thenReturn(Optional.of(ticket));
    }

    private void stubMembership() {
        ProjectMember pm = new ProjectMember();
        pm.setId(UUID.randomUUID());
        when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId)).thenReturn(Optional.of(pm));
    }

    @Nested
    class GetAll {

        @Test
        void shouldReturnAssignments() {
            stubTicket();
            stubMembership();
            MemberTicket mt = new MemberTicket();
            mt.setId(UUID.randomUUID());
            when(memberTicketRepository.findAllActiveByTicketId(ticketId)).thenReturn(List.of(mt));
            when(mapper.toResponse(mt)).thenReturn(stubResponse(mt.getId()));

            List<MemberTicketResponse> result = service.getAll(ticketId, userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenTicketNotFound() {
            when(ticketRepository.findActiveById(ticketId)).thenReturn(Optional.empty());

            assertThrows(TicketNotFoundException.class, () -> service.getAll(ticketId, userId));
        }

        @Test
        void shouldThrowWhenNotMember() {
            stubTicket();
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.getAll(ticketId, userId));
        }
    }

    @Nested
    class Assign {

        @Test
        void shouldAssignMemberToTicket() {
            stubTicket();
            stubMembership();

            ProjectMember pm = new ProjectMember();
            pm.setId(memberId);
            when(projectMemberRepository.findActiveByIdAndProjectId(memberId, projectId))
                    .thenReturn(Optional.of(pm));
            when(memberTicketRepository.findActiveByTicketAndMember(ticketId, memberId))
                    .thenReturn(Optional.empty());

            MemberTicket saved = new MemberTicket();
            saved.setId(UUID.randomUUID());
            when(memberTicketRepository.save(any(MemberTicket.class))).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(stubResponse(saved.getId()));

            MemberTicketResponse result = service.assign(ticketId, new MemberTicketRequest(memberId), userId);

            assertNotNull(result);
        }

        @Test
        void shouldThrowWhenMemberNotInProject() {
            stubTicket();
            stubMembership();
            when(projectMemberRepository.findActiveByIdAndProjectId(memberId, projectId))
                    .thenReturn(Optional.empty());

            MemberTicketRequest request = new MemberTicketRequest(memberId);

            assertThrows(ProjectMemberNotFoundException.class,
                    () -> service.assign(ticketId, request, userId));
            verify(memberTicketRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenAlreadyAssigned() {
            stubTicket();
            stubMembership();

            ProjectMember pm = new ProjectMember();
            pm.setId(memberId);
            when(projectMemberRepository.findActiveByIdAndProjectId(memberId, projectId))
                    .thenReturn(Optional.of(pm));

            MemberTicket existing = new MemberTicket();
            when(memberTicketRepository.findActiveByTicketAndMember(ticketId, memberId))
                    .thenReturn(Optional.of(existing));

            MemberTicketRequest request = new MemberTicketRequest(memberId);

            assertThrows(IllegalArgumentException.class,
                    () -> service.assign(ticketId, request, userId));
            verify(memberTicketRepository, never()).save(any());
        }
    }

    @Nested
    class Unassign {

        @Test
        void shouldSoftDeleteAssignment() {
            stubTicket();
            stubMembership();

            UUID assignmentId = UUID.randomUUID();
            MemberTicket mt = new MemberTicket();
            mt.setId(assignmentId);
            when(memberTicketRepository.findActiveByIdAndTicketId(assignmentId, ticketId))
                    .thenReturn(Optional.of(mt));
            when(memberTicketRepository.save(mt)).thenReturn(mt);
            when(mapper.toResponse(mt)).thenReturn(stubResponse(assignmentId));

            MemberTicketResponse result = service.unassign(ticketId, assignmentId, userId);

            assertNotNull(result);
            assertNotNull(mt.getDeletedAt());
        }

        @Test
        void shouldThrowWhenAssignmentNotFound() {
            stubTicket();
            stubMembership();

            UUID assignmentId = UUID.randomUUID();
            when(memberTicketRepository.findActiveByIdAndTicketId(assignmentId, ticketId))
                    .thenReturn(Optional.empty());

            assertThrows(MemberTicketNotFoundException.class,
                    () -> service.unassign(ticketId, assignmentId, userId));
        }
    }
}
