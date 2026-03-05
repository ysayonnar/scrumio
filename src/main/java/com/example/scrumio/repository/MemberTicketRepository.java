package com.example.scrumio.repository;

import com.example.scrumio.entity.ticket.MemberTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberTicketRepository extends JpaRepository<MemberTicket, UUID> {

    @Query("SELECT mt FROM MemberTicket mt JOIN FETCH mt.member pm JOIN FETCH pm.user "
            + "WHERE mt.ticket.id = :ticketId AND mt.deletedAt IS NULL")
    List<MemberTicket> findAllActiveByTicketId(@Param("ticketId") UUID ticketId);

    @Query("SELECT mt FROM MemberTicket mt "
            + "WHERE mt.id = :id AND mt.ticket.id = :ticketId AND mt.deletedAt IS NULL")
    Optional<MemberTicket> findActiveByIdAndTicketId(@Param("id") UUID id, @Param("ticketId") UUID ticketId);

    @Query("SELECT mt FROM MemberTicket mt "
            + "WHERE mt.ticket.id = :ticketId AND mt.member.id = :memberId AND mt.deletedAt IS NULL")
    Optional<MemberTicket> findActiveByTicketAndMember(@Param("ticketId") UUID ticketId,
                                                       @Param("memberId") UUID memberId);
}
