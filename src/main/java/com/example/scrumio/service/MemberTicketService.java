package com.example.scrumio.service;

import com.example.scrumio.entity.project.ProjectMember;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MemberTicketService {

    private final MemberTicketRepository memberTicketRepository;
    private final TicketRepository ticketRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemberTicketMapper mapper;

    public MemberTicketService(MemberTicketRepository memberTicketRepository,
                               TicketRepository ticketRepository,
                               ProjectMemberRepository projectMemberRepository,
                               MemberTicketMapper mapper) {
        this.memberTicketRepository = memberTicketRepository;
        this.ticketRepository = ticketRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MemberTicketResponse> getAll(UUID ticketId, UUID userId) {
        Ticket ticket = findTicket(ticketId);
        verifyMembership(ticket, userId);
        return memberTicketRepository.findAllActiveByTicketId(ticketId).stream()
                .map(mapper::toResponse).toList();
    }

    public MemberTicketResponse assign(UUID ticketId, MemberTicketRequest request, UUID userId) {
        Ticket ticket = findTicket(ticketId);
        verifyMembership(ticket, userId);

        ProjectMember member = projectMemberRepository
                .findActiveByIdAndProjectId(request.memberId(), ticket.getProject().getId())
                .orElseThrow(() -> new ProjectMemberNotFoundException(request.memberId()));

        memberTicketRepository.findActiveByTicketAndMember(ticketId, request.memberId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Member is already assigned to this ticket");
                });

        MemberTicket mt = new MemberTicket();
        mt.setTicket(ticket);
        mt.setMember(member);
        return mapper.toResponse(memberTicketRepository.save(mt));
    }

    public MemberTicketResponse unassign(UUID ticketId, UUID assignmentId, UUID userId) {
        Ticket ticket = findTicket(ticketId);
        verifyMembership(ticket, userId);

        MemberTicket mt = memberTicketRepository.findActiveByIdAndTicketId(assignmentId, ticketId)
                .orElseThrow(() -> new MemberTicketNotFoundException(assignmentId));
        mt.setDeletedAt(OffsetDateTime.now());
        return mapper.toResponse(memberTicketRepository.save(mt));
    }

    private Ticket findTicket(UUID ticketId) {
        return ticketRepository.findActiveById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    private void verifyMembership(Ticket ticket, UUID userId) {
        projectMemberRepository
                .findActiveByProjectAndUser(ticket.getProject().getId(), userId)
                .orElseThrow(() -> new ProjectNotFoundException(ticket.getProject().getId()));
    }
}
