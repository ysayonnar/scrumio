package com.example.scrumio.repository;

import com.example.scrumio.entity.sprint.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    // N+1 FIX: JOIN FETCH loads project in the same query instead of triggering a SELECT per sprint
    @Query("SELECT s FROM Sprint s JOIN FETCH s.project WHERE s.deletedAt IS NULL AND s.project.id = :projectId")
    List<Sprint> findAllActiveByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT s FROM Sprint s JOIN FETCH s.project WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Sprint> findActiveById(@Param("id") UUID id);

    @Query("SELECT s FROM Sprint s JOIN FETCH s.project WHERE s.id = :id AND s.deletedAt IS NULL "
            + "AND (s.project.owner.id = :userId OR EXISTS "
            + "(SELECT pm FROM ProjectMember pm WHERE pm.project = s.project AND pm.user.id = :userId AND pm.deletedAt IS NULL))")
    Optional<Sprint> findActiveByIdForUser(@Param("id") UUID id, @Param("userId") UUID userId);
}
