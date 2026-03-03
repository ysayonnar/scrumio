package com.example.scrumio.repository;

import com.example.scrumio.entity.meeting.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    // N+1 FIX: JOIN FETCH loads project + sprint in the same query
    @Query("SELECT m FROM Meeting m JOIN FETCH m.project LEFT JOIN FETCH m.sprint "
            + "WHERE m.deletedAt IS NULL AND m.project.id = :projectId")
    List<Meeting> findAllActiveByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT m FROM Meeting m JOIN FETCH m.project LEFT JOIN FETCH m.sprint "
            + "WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<Meeting> findActiveById(@Param("id") UUID id);

    @Query("SELECT m FROM Meeting m JOIN FETCH m.project LEFT JOIN FETCH m.sprint "
            + "WHERE m.id = :id AND m.deletedAt IS NULL "
            + "AND (m.project.owner.id = :userId OR EXISTS "
            + "(SELECT pm FROM ProjectMember pm WHERE pm.project = m.project AND pm.user.id = :userId AND pm.deletedAt IS NULL))")
    Optional<Meeting> findActiveByIdForUser(@Param("id") UUID id, @Param("userId") UUID userId);
}
