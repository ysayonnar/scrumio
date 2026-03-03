package com.example.scrumio.repository;

import com.example.scrumio.entity.meeting.MeetingMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MeetingMemberRepository extends JpaRepository<MeetingMember, UUID> {
}
