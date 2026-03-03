package com.example.scrumio.repository;

import com.example.scrumio.entity.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL "
            + "AND (p.owner.id = :userId OR EXISTS "
            + "(SELECT pm FROM ProjectMember pm WHERE pm.project = p AND pm.user.id = :userId AND pm.deletedAt IS NULL))")
    List<Project> findAllActiveUserProjects(@Param("userId") UUID userId);

    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Project> findActiveById(@Param("id") UUID id);

    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.deletedAt IS NULL "
            + "AND (p.owner.id = :userId OR EXISTS "
            + "(SELECT pm FROM ProjectMember pm WHERE pm.project = p AND pm.user.id = :userId AND pm.deletedAt IS NULL))")
    Optional<Project> findActiveByIdForUser(@Param("id") UUID id, @Param("userId") UUID userId);
}
