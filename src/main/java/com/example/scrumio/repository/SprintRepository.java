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

    @Query("SELECT s FROM Sprint s WHERE s.deletedAt IS NULL")
    List<Sprint> findAllActive();

    @Query("SELECT s FROM Sprint s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Sprint> findActiveById(@Param("id") UUID id);
}
