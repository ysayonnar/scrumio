package com.example.scrumio.repository;

import java.util.*;

import com.example.scrumio.entity.Ticket;
import com.example.scrumio.entity.TicketPriority;
import com.example.scrumio.entity.TicketStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class TicketRepository {
    private final Map<UUID, Ticket> tickets = new HashMap<>();

    public Ticket save(Ticket ticket) {
        if (ticket.getId() == null) {
           ticket.setId(UUID.randomUUID());
           ticket.setCreatedAt(LocalDateTime.now());
        } else {
            ticket.setUpdatedAt(LocalDateTime.now());
        }

        tickets.put(ticket.getId(), ticket);
        return ticket;
    }

    public List<Ticket> findAll(TicketStatus status, TicketPriority priority) {
        return tickets.values()
                .stream()
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> priority == null || t.getPriority() == priority)
                .toList();
    }

    public Optional<Ticket> findByID(UUID id) {
        Ticket ticket = tickets.get(id);
        if(ticket == null || ticket.getDeletedAt() != null){
            return Optional.empty();
        }

        return Optional.of(ticket);
    }

    public Optional<Ticket> deleteByID(UUID id){
        Ticket ticket = tickets.get(id);
        if(ticket == null || ticket.getDeletedAt() != null){
            return Optional.empty();
        }

        ticket.setDeletedAt(LocalDateTime.now());
        tickets.put(id, ticket);

        return Optional.of(ticket);
    }
}
