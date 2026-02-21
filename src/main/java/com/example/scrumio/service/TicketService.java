package com.example.scrumio.service;

import com.example.scrumio.dto.TicketRequest;
import com.example.scrumio.dto.TicketResponse;
import com.example.scrumio.entity.Ticket;
import com.example.scrumio.mapper.TicketMapper;
import com.example.scrumio.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// TODO: add more complex business logic
// TODO: refactor errors
@Service
public class TicketService {
    private final TicketRepository repository;
    private final TicketMapper mapper;

    public TicketService(TicketRepository repository, TicketMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // TODO: maybe add dto for list
    public List<TicketResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public TicketResponse getByID(UUID id) {
        Ticket ticket = repository.findByID(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return mapper.toResponse(ticket);
    }

    public TicketResponse create(TicketRequest request) {
        Ticket ticket = mapper.toEntity(request);
        Ticket savedTicket = repository.save(ticket);
        return mapper.toResponse(savedTicket);
    }

    public TicketResponse update(UUID id, TicketRequest request) {
        Ticket ticket = mapper.toEntity(request);
        ticket.setId(id);
        Ticket savedTicket = repository.save(ticket);
        return mapper.toResponse(savedTicket);
    }

    public TicketResponse delete(UUID id) {
        Ticket deletedTicket = repository.deleteByID(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return mapper.toResponse(deletedTicket);
    }
}
