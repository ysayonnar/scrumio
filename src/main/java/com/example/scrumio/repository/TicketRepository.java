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

    // N+1 FIX: JOIN FETCH loads project + sprint in the same query
    @Query("SELECT t FROM Ticket t JOIN FETCH t.project LEFT JOIN FETCH t.sprint "
            + "WHERE t.deletedAt IS NULL AND t.project.id = :projectId "
            + "AND (CAST(:status AS String) IS NULL OR t.status = :status) "
            + "AND (CAST(:priority AS String) IS NULL OR t.priority = :priority)")
    List<Ticket> findAllActiveByProjectId(@Param("projectId") UUID projectId,
                                          @Param("status") TicketStatus status,
                                          @Param("priority") TicketPriority priority);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.project LEFT JOIN FETCH t.sprint "
            + "WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Ticket> findActiveById(@Param("id") UUID id);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.project LEFT JOIN FETCH t.sprint "
            + "WHERE t.id = :id AND t.deletedAt IS NULL "
            + "AND (t.project.owner.id = :userId OR EXISTS "
            + "(SELECT pm FROM ProjectMember pm WHERE pm.project = t.project AND pm.user.id = :userId AND pm.deletedAt IS NULL))")
    Optional<Ticket> findActiveByIdForUser(@Param("id") UUID id, @Param("userId") UUID userId);
}
