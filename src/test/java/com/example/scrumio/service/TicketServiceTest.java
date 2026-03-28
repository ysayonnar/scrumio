package com.example.scrumio.service;

import com.example.scrumio.cache.TicketCacheIndex;
import com.example.scrumio.cache.TicketCacheKey;
import com.example.scrumio.entity.project.Project;
import com.example.scrumio.entity.project.ProjectMember;
import com.example.scrumio.entity.sprint.Sprint;
import com.example.scrumio.entity.sprint.SprintStatus;
import com.example.scrumio.entity.ticket.MemberTicket;
import com.example.scrumio.entity.ticket.Ticket;
import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import com.example.scrumio.mapper.TicketMapper;
import com.example.scrumio.repository.MemberTicketRepository;
import com.example.scrumio.repository.ProjectMemberRepository;
import com.example.scrumio.repository.ProjectRepository;
import com.example.scrumio.repository.SprintRepository;
import com.example.scrumio.repository.TicketRepository;
import com.example.scrumio.web.dto.BulkTicketItemRequest;
import com.example.scrumio.web.dto.BulkTicketRequest;
import com.example.scrumio.web.dto.TicketPatchRequest;
import com.example.scrumio.web.dto.TicketRequest;
import com.example.scrumio.web.dto.TicketResponse;
import com.example.scrumio.web.exception.ProjectMemberNotFoundException;
import com.example.scrumio.web.exception.ProjectNotFoundException;
import com.example.scrumio.web.exception.SprintNotFoundException;
import com.example.scrumio.web.exception.TicketNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private SprintRepository sprintRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private MemberTicketRepository memberTicketRepository;
    @Mock
    private TicketMapper mapper;
    @Mock
    private TicketCacheIndex cacheIndex;

    @InjectMocks
    private TicketService service;

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
        project.setName("Test Project");

        sprint = new Sprint();
        sprint.setId(sprintId);
        sprint.setProject(project);
    }

    private Ticket stubTicket(UUID id) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("Ticket " + id);
        ticket.setStatus(TicketStatus.TODO);
        ticket.setProject(project);
        ticket.setCreatedAt(OffsetDateTime.now());
        return ticket;
    }

    private TicketResponse stubResponse(UUID id) {
        return new TicketResponse(id, "Ticket " + id, null, null, TicketStatus.TODO,
                null, null, null, projectId, OffsetDateTime.now());
    }

    private ProjectMember stubMember(UUID id) {
        ProjectMember pm = new ProjectMember();
        pm.setId(id);
        return pm;
    }

    private void stubMembership() {
        when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                .thenReturn(Optional.of(stubMember(UUID.randomUUID())));
    }

    @Nested
    class GetAll {

        @Test
        void shouldReturnCachedPage() {
            stubMembership();
            Pageable pageable = PageRequest.of(0, 10);
            Page<TicketResponse> cached = new PageImpl<>(List.of(stubResponse(UUID.randomUUID())));
            when(cacheIndex.get(any(TicketCacheKey.class))).thenReturn(Optional.of(cached));

            Page<TicketResponse> result = service.getAll(projectId, userId, null, null, null, pageable);

            assertEquals(cached, result);
            verify(ticketRepository, never()).findAllActiveByProjectId(any(), any(), any(), any(), any());
        }

        @Test
        void shouldQueryAndCacheOnCacheMiss() {
            stubMembership();
            Pageable pageable = PageRequest.of(0, 10);
            Ticket ticket = stubTicket(UUID.randomUUID());
            Page<Ticket> dbPage = new PageImpl<>(List.of(ticket));
            when(cacheIndex.get(any(TicketCacheKey.class))).thenReturn(Optional.empty());
            when(ticketRepository.findAllActiveByProjectId(eq(projectId), any(), any(), any(), eq(pageable)))
                    .thenReturn(dbPage);
            when(mapper.toResponse(ticket)).thenReturn(stubResponse(ticket.getId()));

            Page<TicketResponse> result = service.getAll(projectId, userId, TicketStatus.TODO, TicketPriority.HIGH, SprintStatus.ACTIVE, pageable);

            assertNotNull(result);
            verify(cacheIndex).put(any(TicketCacheKey.class), any());
        }

        @Test
        void shouldThrowWhenNotMember() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class,
                    () -> service.getAll(projectId, userId, null, null, null, PageRequest.of(0, 10)));
        }
    }

    @Nested
    class GetAllNative {

        @Test
        void shouldReturnNativePage() {
            stubMembership();
            Pageable pageable = PageRequest.of(0, 10);
            when(ticketRepository.findAllActiveByProjectIdNative(eq(projectId), any(), any(), any(), eq(pageable)))
                    .thenReturn(Page.empty());

            Page<TicketResponse> result = service.getAllNative(projectId, userId, TicketStatus.TODO, TicketPriority.HIGH, SprintStatus.ACTIVE, pageable);

            assertNotNull(result);
        }

        @Test
        void shouldHandleNullFilters() {
            stubMembership();
            Pageable pageable = PageRequest.of(0, 10);
            when(ticketRepository.findAllActiveByProjectIdNative(eq(projectId), eq(null), eq(null), eq(null), eq(pageable)))
                    .thenReturn(Page.empty());

            Page<TicketResponse> result = service.getAllNative(projectId, userId, null, null, null, pageable);

            assertNotNull(result);
        }
    }

    @Nested
    class GetAllSafe {

        @Test
        void shouldReturnSafeList() {
            stubMembership();
            Ticket ticket = stubTicket(UUID.randomUUID());
            when(ticketRepository.findAllActiveByProjectIdSafe(projectId)).thenReturn(List.of(ticket));
            when(mapper.toResponse(ticket)).thenReturn(stubResponse(ticket.getId()));

            List<TicketResponse> result = service.getAllSafe(projectId, userId);

            assertEquals(1, result.size());
        }

        @Test
        void shouldThrowWhenNotMember() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.getAllSafe(projectId, userId));
        }
    }

    @Nested
    class GetAllUnsafe {

        @Test
        void shouldReturnUnsafeList() {
            stubMembership();
            Ticket ticket = stubTicket(UUID.randomUUID());
            when(ticketRepository.findAllActiveByProjectIdUnsafe(projectId)).thenReturn(List.of(ticket));
            when(mapper.toResponse(ticket)).thenReturn(stubResponse(ticket.getId()));

            List<TicketResponse> result = service.getAllUnsafe(projectId, userId);

            assertEquals(1, result.size());
        }

        @Test
        void shouldThrowWhenNotMember() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.getAllUnsafe(projectId, userId));
        }
    }

    @Nested
    class Create {

        @Test
        void shouldCreateTicketWithoutSprint() {
            stubMembership();
            TicketRequest request = new TicketRequest("New ticket", "desc", TicketPriority.HIGH,
                    TicketStatus.TODO, 3, null, projectId);
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            Ticket saved = stubTicket(UUID.randomUUID());
            when(ticketRepository.save(any(Ticket.class))).thenReturn(saved);
            TicketResponse expected = stubResponse(saved.getId());
            when(mapper.toResponse(saved)).thenReturn(expected);

            TicketResponse result = service.create(request, userId);

            assertEquals(expected, result);
            verify(ticketRepository).save(any(Ticket.class));
            verify(sprintRepository, never()).findActiveById(any());
        }

        @Test
        void shouldCreateTicketWithSprint() {
            stubMembership();
            TicketRequest request = new TicketRequest("New ticket", "desc", TicketPriority.HIGH,
                    TicketStatus.TODO, 3, sprintId, projectId);
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(sprintRepository.findActiveById(sprintId)).thenReturn(Optional.of(sprint));
            Ticket saved = stubTicket(UUID.randomUUID());
            when(ticketRepository.save(any(Ticket.class))).thenReturn(saved);
            TicketResponse expected = stubResponse(saved.getId());
            when(mapper.toResponse(saved)).thenReturn(expected);

            TicketResponse result = service.create(request, userId);

            assertEquals(expected, result);
            verify(sprintRepository).findActiveById(sprintId);
        }

        @Test
        void shouldThrowWhenProjectNotFound() {
            stubMembership();
            TicketRequest request = new TicketRequest("New ticket", null, null,
                    TicketStatus.TODO, null, null, projectId);
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> service.create(request, userId));
        }

        @Test
        void shouldThrowWhenNotMember() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());
            TicketRequest request = new TicketRequest("New ticket", null, null,
                    TicketStatus.TODO, null, null, projectId);

            assertThrows(ProjectNotFoundException.class, () -> service.create(request, userId));
            verify(ticketRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenSprintNotFound() {
            stubMembership();
            UUID badSprintId = UUID.randomUUID();
            TicketRequest request = new TicketRequest("New ticket", null, null,
                    TicketStatus.TODO, null, badSprintId, projectId);
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(sprintRepository.findActiveById(badSprintId)).thenReturn(Optional.empty());

            assertThrows(SprintNotFoundException.class, () -> service.create(request, userId));
        }

        @Test
        void shouldThrowWhenSprintBelongsToDifferentProject() {
            stubMembership();
            Project otherProject = new Project();
            otherProject.setId(UUID.randomUUID());
            Sprint otherSprint = new Sprint();
            otherSprint.setId(sprintId);
            otherSprint.setProject(otherProject);

            TicketRequest request = new TicketRequest("New ticket", null, null,
                    TicketStatus.TODO, null, sprintId, projectId);
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(sprintRepository.findActiveById(sprintId)).thenReturn(Optional.of(otherSprint));

            assertThrows(IllegalArgumentException.class, () -> service.create(request, userId));
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnTicket() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = stubTicket(ticketId);
            TicketResponse expected = stubResponse(ticketId);
            when(ticketRepository.findActiveByIdForUser(ticketId, userId)).thenReturn(Optional.of(ticket));
            when(mapper.toResponse(ticket)).thenReturn(expected);

            TicketResponse result = service.getByID(ticketId, userId);

            assertEquals(expected, result);
        }

        @Test
        void shouldThrowWhenTicketNotFound() {
            UUID ticketId = UUID.randomUUID();
            when(ticketRepository.findActiveByIdForUser(ticketId, userId)).thenReturn(Optional.empty());

            assertThrows(TicketNotFoundException.class, () -> service.getByID(ticketId, userId));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateTicket() {
            UUID ticketId = UUID.randomUUID();
            Ticket existing = stubTicket(ticketId);
            when(ticketRepository.findActiveByIdForUser(ticketId, userId)).thenReturn(Optional.of(existing));
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            Ticket saved = stubTicket(ticketId);
            when(ticketRepository.save(any(Ticket.class))).thenReturn(saved);
            TicketResponse expected = stubResponse(ticketId);
            when(mapper.toResponse(saved)).thenReturn(expected);

            TicketRequest request = new TicketRequest("Updated", "desc", TicketPriority.LOW,
                    TicketStatus.IN_PROGRESS, 5, null, projectId);
            TicketResponse result = service.update(ticketId, request, userId);

            assertEquals(expected, result);
        }

        @Test
        void shouldThrowWhenTicketNotFoundOnUpdate() {
            UUID ticketId = UUID.randomUUID();
            when(ticketRepository.findActiveByIdForUser(ticketId, userId)).thenReturn(Optional.empty());

            TicketRequest request = new TicketRequest("Updated", null, null,
                    TicketStatus.TODO, null, null, projectId);
            assertThrows(TicketNotFoundException.class, () -> service.update(ticketId, request, userId));
        }
    }

    @Nested
    class Patch {

        @Test
        void shouldPatchOnlyProvidedFields() {
            UUID ticketId = UUID.randomUUID();
            Ticket existing = stubTicket(ticketId);
            existing.setTitle("Original");
            existing.setDescription("Original desc");
            when(ticketRepository.findActiveByIdForUser(ticketId, userId)).thenReturn(Optional.of(existing));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(existing);
            TicketResponse expected = stubResponse(ticketId);
            when(mapper.toResponse(existing)).thenReturn(expected);

            TicketPatchRequest request = new TicketPatchRequest("Patched", null, null, null, null, null);
            service.patch(ticketId, request, userId);

            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(captor.capture());
            assertEquals("Patched", captor.getValue().getTitle());
            assertEquals("Original desc", captor.getValue().getDescription());
        }

        @Test
        void shouldPatchAllRemainingFields() {
            UUID ticketId = UUID.randomUUID();
            Ticket existing = stubTicket(ticketId);
            existing.setProject(project);
            when(ticketRepository.findActiveByIdForUser(ticketId, userId)).thenReturn(Optional.of(existing));
            when(sprintRepository.findActiveById(sprintId)).thenReturn(Optional.of(sprint));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(existing);
            when(mapper.toResponse(existing)).thenReturn(stubResponse(ticketId));

            TicketPatchRequest request = new TicketPatchRequest(null, "desc", TicketPriority.LOW, TicketStatus.IN_PROGRESS, 5, sprintId);
            service.patch(ticketId, request, userId);

            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(captor.capture());
            assertEquals("desc", captor.getValue().getDescription());
            assertEquals(TicketPriority.LOW, captor.getValue().getPriority());
            assertEquals(TicketStatus.IN_PROGRESS, captor.getValue().getStatus());
            assertEquals(5, captor.getValue().getEstimation());
            assertEquals(sprint, captor.getValue().getSprint());
        }

        @Test
        void shouldThrowWhenNotFoundOnPatch() {
            UUID ticketId = UUID.randomUUID();
            when(ticketRepository.findActiveByIdForUser(ticketId, userId)).thenReturn(Optional.empty());

            assertThrows(TicketNotFoundException.class,
                    () -> service.patch(ticketId, new TicketPatchRequest(null, null, null, null, null, null), userId));
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldSoftDeleteTicketAndMemberTickets() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = stubTicket(ticketId);
            when(ticketRepository.findActiveByIdForUser(ticketId, userId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
            TicketResponse expected = stubResponse(ticketId);
            when(mapper.toResponse(ticket)).thenReturn(expected);

            TicketResponse result = service.delete(ticketId, userId);

            assertNotNull(result);
            verify(memberTicketRepository).softDeleteAllActiveByTicketId(eq(ticketId), any(OffsetDateTime.class));
            assertNotNull(ticket.getDeletedAt());
        }
    }

    @Nested
    class BulkCreate {

        @Test
        void shouldCreateMultipleTicketsWithMembers() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));
            when(sprintRepository.findActiveById(sprintId)).thenReturn(Optional.of(sprint));

            UUID member1 = UUID.randomUUID();
            UUID member2 = UUID.randomUUID();
            ProjectMember pm1 = stubMember(member1);
            ProjectMember pm2 = stubMember(member2);
            when(projectMemberRepository.findAllActiveByIdsAndProjectId(any(), eq(projectId)))
                    .thenReturn(List.of(pm1, pm2));

            BulkTicketItemRequest item1 = new BulkTicketItemRequest(
                    "Ticket 1", "desc1", TicketPriority.HIGH, TicketStatus.TODO, 3, List.of(member1));
            BulkTicketItemRequest item2 = new BulkTicketItemRequest(
                    "Ticket 2", "desc2", TicketPriority.LOW, TicketStatus.BACKLOG, 1, List.of(member1, member2));
            BulkTicketRequest request = new BulkTicketRequest(projectId, sprintId, List.of(item1, item2));

            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                t.setId(UUID.randomUUID());
                t.setCreatedAt(OffsetDateTime.now());
                return t;
            });
            when(memberTicketRepository.save(any(MemberTicket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toResponse(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                return new TicketResponse(t.getId(), t.getTitle(), t.getDescription(),
                        t.getPriority(), t.getStatus(), t.getEstimation(),
                        sprintId, sprint.getName(), projectId, t.getCreatedAt());
            });

            List<TicketResponse> result = service.createBulk(request, userId);

            assertEquals(2, result.size());
            assertEquals("Ticket 1", result.get(0).title());
            assertEquals("Ticket 2", result.get(1).title());
            verify(ticketRepository, times(2)).save(any(Ticket.class));
            verify(memberTicketRepository, times(3)).save(any(MemberTicket.class));
            verify(projectMemberRepository).findAllActiveByIdsAndProjectId(any(), eq(projectId));
        }

        @Test
        void shouldCreateTicketsWithoutMembers() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));

            BulkTicketItemRequest item = new BulkTicketItemRequest(
                    "Ticket 1", null, null, TicketStatus.BACKLOG, null, null);
            BulkTicketRequest request = new BulkTicketRequest(projectId, null, List.of(item));

            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                t.setId(UUID.randomUUID());
                t.setCreatedAt(OffsetDateTime.now());
                return t;
            });
            when(mapper.toResponse(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                return new TicketResponse(t.getId(), t.getTitle(), null, null, t.getStatus(),
                        null, null, null, projectId, t.getCreatedAt());
            });

            List<TicketResponse> result = service.createBulk(request, userId);

            assertEquals(1, result.size());
            verify(memberTicketRepository, never()).save(any());
            verify(projectMemberRepository, never()).findAllActiveByIdsAndProjectId(any(), any());
        }

        @Test
        void shouldThrowWhenMemberNotFoundInBulk() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));

            UUID validMember = UUID.randomUUID();
            UUID invalidMember = UUID.randomUUID();
            when(projectMemberRepository.findAllActiveByIdsAndProjectId(any(), eq(projectId)))
                    .thenReturn(List.of(stubMember(validMember)));

            BulkTicketItemRequest item = new BulkTicketItemRequest(
                    "Ticket 1", null, null, TicketStatus.TODO, null, List.of(validMember, invalidMember));
            BulkTicketRequest request = new BulkTicketRequest(projectId, null, List.of(item));

            assertThrows(ProjectMemberNotFoundException.class,
                    () -> service.createBulk(request, userId));
            verify(ticketRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenNotMemberInBulk() {
            when(projectMemberRepository.findActiveByProjectAndUser(projectId, userId))
                    .thenReturn(Optional.empty());

            BulkTicketItemRequest item = new BulkTicketItemRequest(
                    "Ticket 1", null, null, TicketStatus.TODO, null, null);
            BulkTicketRequest request = new BulkTicketRequest(projectId, null, List.of(item));

            assertThrows(ProjectNotFoundException.class,
                    () -> service.createBulk(request, userId));
            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    class BulkCreateUnsafe {

        @Test
        void shouldCreateTicketsOneByOneWithN1Queries() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));

            UUID member1 = UUID.randomUUID();
            UUID member2 = UUID.randomUUID();
            ProjectMember pm1 = stubMember(member1);
            ProjectMember pm2 = stubMember(member2);

            when(projectMemberRepository.findActiveByIdAndProjectId(member1, projectId))
                    .thenReturn(Optional.of(pm1));
            when(projectMemberRepository.findActiveByIdAndProjectId(member2, projectId))
                    .thenReturn(Optional.of(pm2));

            BulkTicketItemRequest item1 = new BulkTicketItemRequest(
                    "Ticket 1", null, TicketPriority.HIGH, TicketStatus.TODO, 3, List.of(member1));
            BulkTicketItemRequest item2 = new BulkTicketItemRequest(
                    "Ticket 2", null, null, TicketStatus.BACKLOG, null, List.of(member2));
            BulkTicketRequest request = new BulkTicketRequest(projectId, null, List.of(item1, item2));

            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                t.setId(UUID.randomUUID());
                t.setCreatedAt(OffsetDateTime.now());
                return t;
            });
            when(memberTicketRepository.save(any(MemberTicket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toResponse(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                return new TicketResponse(t.getId(), t.getTitle(), null, t.getPriority(), t.getStatus(),
                        t.getEstimation(), null, null, projectId, t.getCreatedAt());
            });

            List<TicketResponse> result = service.createBulkUnsafe(request, userId);

            assertEquals(2, result.size());
            verify(projectMemberRepository).findActiveByIdAndProjectId(member1, projectId);
            verify(projectMemberRepository).findActiveByIdAndProjectId(member2, projectId);
            verify(projectMemberRepository, never()).findAllActiveByIdsAndProjectId(any(), any());
        }

        @Test
        void shouldPartiallyCreateWhenMemberNotFoundInUnsafe() {
            stubMembership();
            when(projectRepository.findActiveById(projectId)).thenReturn(Optional.of(project));

            UUID validMember = UUID.randomUUID();
            UUID invalidMember = UUID.randomUUID();
            when(projectMemberRepository.findActiveByIdAndProjectId(validMember, projectId))
                    .thenReturn(Optional.of(stubMember(validMember)));
            when(projectMemberRepository.findActiveByIdAndProjectId(invalidMember, projectId))
                    .thenReturn(Optional.empty());

            BulkTicketItemRequest item1 = new BulkTicketItemRequest(
                    "Ticket 1", null, null, TicketStatus.TODO, null, List.of(validMember));
            BulkTicketItemRequest item2 = new BulkTicketItemRequest(
                    "Ticket 2", null, null, TicketStatus.TODO, null, List.of(invalidMember));
            BulkTicketRequest request = new BulkTicketRequest(projectId, null, List.of(item1, item2));

            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                t.setId(UUID.randomUUID());
                t.setCreatedAt(OffsetDateTime.now());
                return t;
            });
            when(memberTicketRepository.save(any(MemberTicket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toResponse(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                return new TicketResponse(t.getId(), t.getTitle(), null, null, t.getStatus(),
                        null, null, null, projectId, t.getCreatedAt());
            });

            assertThrows(ProjectMemberNotFoundException.class,
                    () -> service.createBulkUnsafe(request, userId));
            verify(ticketRepository, times(2)).save(any(Ticket.class));
        }
    }
}
