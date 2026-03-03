package com.example.scrumio.repository;

import com.example.scrumio.entity.project.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
