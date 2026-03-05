package com.example.scrumio.repository;

import com.example.scrumio.entity.meeting.MeetingMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface MeetingMemberRepository extends JpaRepository<MeetingMember, UUID> {

    @Modifying
    @Query("UPDATE MeetingMember mm SET mm.deletedAt = :now WHERE mm.meeting.id = :meetingId AND mm.deletedAt IS NULL")
    void softDeleteAllActiveByMeetingId(@Param("meetingId") UUID meetingId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE MeetingMember mm SET mm.deletedAt = :now WHERE mm.member.id = :memberId AND mm.deletedAt IS NULL")
    void softDeleteAllActiveByMemberId(@Param("memberId") UUID memberId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE MeetingMember mm SET mm.deletedAt = :now WHERE mm.meeting.project.id = :projectId AND mm.deletedAt IS NULL")
    void softDeleteAllActiveByProjectId(@Param("projectId") UUID projectId, @Param("now") OffsetDateTime now);
}
