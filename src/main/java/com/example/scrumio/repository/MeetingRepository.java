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

    // N+1 FIX: JOIN FETCH loads project + sprint + members + user in the same query
    @Query("SELECT DISTINCT m FROM Meeting m JOIN FETCH m.project LEFT JOIN FETCH m.sprint "
            + "LEFT JOIN FETCH m.members mm LEFT JOIN FETCH mm.member pm LEFT JOIN FETCH pm.user "
            + "WHERE m.deletedAt IS NULL AND m.project.id = :projectId "
            + "AND (mm IS NULL OR mm.deletedAt IS NULL)")
    List<Meeting> findAllActiveByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT m FROM Meeting m JOIN FETCH m.project LEFT JOIN FETCH m.sprint "
            + "LEFT JOIN FETCH m.members mm LEFT JOIN FETCH mm.member pm LEFT JOIN FETCH pm.user "
            + "WHERE m.id = :id AND m.deletedAt IS NULL "
            + "AND (mm IS NULL OR mm.deletedAt IS NULL)")
    Optional<Meeting> findActiveById(@Param("id") UUID id);

    @Query("SELECT m FROM Meeting m JOIN FETCH m.project LEFT JOIN FETCH m.sprint "
            + "LEFT JOIN FETCH m.members mm LEFT JOIN FETCH mm.member pm LEFT JOIN FETCH pm.user "
            + "WHERE m.id = :id AND m.deletedAt IS NULL "
            + "AND (mm IS NULL OR mm.deletedAt IS NULL) "
            + "AND (m.project.owner.id = :userId OR EXISTS "
            + "(SELECT ppm FROM ProjectMember ppm WHERE ppm.project = m.project AND ppm.user.id = :userId AND ppm.deletedAt IS NULL))")
    Optional<Meeting> findActiveByIdForUser(@Param("id") UUID id, @Param("userId") UUID userId);
}
