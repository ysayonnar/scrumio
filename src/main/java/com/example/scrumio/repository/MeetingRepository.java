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

    @Query("SELECT m FROM Meeting m WHERE m.deletedAt IS NULL")
    List<Meeting> findAllActive();

    @Query("SELECT m FROM Meeting m WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<Meeting> findActiveById(@Param("id") UUID id);
}
