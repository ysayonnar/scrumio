package com.example.scrumio.repository;

import com.example.scrumio.entity.ticket.Ticket;
import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query("SELECT t FROM Ticket t WHERE t.deletedAt IS NULL " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority)")
    List<Ticket> findAllActive(@Param("status") TicketStatus status,
                               @Param("priority") TicketPriority priority);

    @Query("SELECT t FROM Ticket t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Ticket> findActiveById(@Param("id") UUID id);
}
