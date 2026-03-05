package com.example.scrumio.repository;

import com.example.scrumio.entity.project.ProjectMember;
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
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user "
            + "WHERE pm.project.id = :projectId AND pm.deletedAt IS NULL")
    List<ProjectMember> findAllActiveByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT pm FROM ProjectMember pm "
            + "WHERE pm.project.id = :projectId AND pm.user.id = :userId AND pm.deletedAt IS NULL")
    Optional<ProjectMember> findActiveByProjectAndUser(@Param("projectId") UUID projectId,
                                                       @Param("userId") UUID userId);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.id = :id AND pm.deletedAt IS NULL")
    Optional<ProjectMember> findActiveById(@Param("id") UUID id);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.id = :id AND pm.project.id = :projectId AND pm.deletedAt IS NULL")
    Optional<ProjectMember> findActiveByIdAndProjectId(@Param("id") UUID id, @Param("projectId") UUID projectId);

    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user "
            + "WHERE pm.id IN :ids AND pm.project.id = :projectId AND pm.deletedAt IS NULL")
    List<ProjectMember> findAllActiveByIdsAndProjectId(@Param("ids") List<UUID> ids,
                                                       @Param("projectId") UUID projectId);

    @Modifying
    @Query("UPDATE ProjectMember pm SET pm.deletedAt = :now WHERE pm.project.id = :projectId AND pm.deletedAt IS NULL")
    void softDeleteAllActiveByProjectId(@Param("projectId") UUID projectId, @Param("now") OffsetDateTime now);
}
