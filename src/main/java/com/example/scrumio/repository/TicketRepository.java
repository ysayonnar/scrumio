package com.example.scrumio.repository;

import com.example.scrumio.entity.sprint.SprintStatus;
import com.example.scrumio.entity.ticket.Ticket;
import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import com.example.scrumio.web.dto.TicketNativeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query("SELECT t FROM Ticket t JOIN FETCH t.project LEFT JOIN FETCH t.sprint s "
            + "WHERE t.deletedAt IS NULL AND t.project.id = :projectId "
            + "AND (CAST(:status AS String) IS NULL OR t.status = :status) "
            + "AND (CAST(:priority AS String) IS NULL OR t.priority = :priority) "
            + "AND (CAST(:sprintStatus AS String) IS NULL OR s.status = :sprintStatus)")
    List<Ticket> findAllActiveByProjectId(@Param("projectId") UUID projectId,
                                          @Param("status") TicketStatus status,
                                          @Param("priority") TicketPriority priority,
                                          @Param("sprintStatus") SprintStatus sprintStatus);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.project LEFT JOIN FETCH t.sprint "
            + "WHERE t.deletedAt IS NULL AND t.project.id = :projectId")
    List<Ticket> findAllActiveByProjectIdSafe(@Param("projectId") UUID projectId);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.project "
            + "WHERE t.deletedAt IS NULL AND t.project.id = :projectId")
    List<Ticket> findAllActiveByProjectIdUnsafe(@Param("projectId") UUID projectId);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.project LEFT JOIN FETCH t.sprint "
            + "WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Ticket> findActiveById(@Param("id") UUID id);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.project LEFT JOIN FETCH t.sprint "
            + "WHERE t.id = :id AND t.deletedAt IS NULL "
            + "AND (t.project.owner.id = :userId OR EXISTS "
            + "(SELECT pm FROM ProjectMember pm WHERE pm.project = t.project AND pm.user.id = :userId AND pm.deletedAt IS NULL))")
    Optional<Ticket> findActiveByIdForUser(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query(value = "SELECT t.id, t.title, t.description, "
            + "CAST(t.priority AS text) AS priority, CAST(t.status AS text) AS status, "
            + "t.estimation, s.id AS sprint_id, s.name AS sprint_name, "
            + "t.project_id, t.created_at "
            + "FROM ticket t "
            + "JOIN project p ON p.id = t.project_id "
            + "LEFT JOIN sprint s ON s.id = t.sprint_id "
            + "WHERE t.deleted_at IS NULL AND t.project_id = :projectId "
            + "AND (:status IS NULL OR t.status = CAST(:status AS ticket_status)) "
            + "AND (:priority IS NULL OR t.priority = CAST(:priority AS ticket_priority)) "
            + "AND (:sprintStatus IS NULL OR s.status = CAST(:sprintStatus AS sprint_status))",
            nativeQuery = true)
    List<TicketNativeProjection> findAllActiveByProjectIdNative(@Param("projectId") UUID projectId,
                                                                @Param("status") String status,
                                                                @Param("priority") String priority,
                                                                @Param("sprintStatus") String sprintStatus);

    @Modifying
    @Query("UPDATE Ticket t SET t.deletedAt = :now WHERE t.project.id = :projectId AND t.deletedAt IS NULL")
    void softDeleteAllActiveByProjectId(@Param("projectId") UUID projectId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE Ticket t SET t.sprint = null WHERE t.sprint.id = :sprintId AND t.deletedAt IS NULL")
    void unlinkFromSprint(@Param("sprintId") UUID sprintId);
}
