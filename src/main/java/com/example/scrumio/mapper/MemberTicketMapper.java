package com.example.scrumio.mapper;

import com.example.scrumio.entity.ticket.MemberTicket;
import com.example.scrumio.web.dto.MemberTicketResponse;
import org.springframework.stereotype.Component;

@Component
public class MemberTicketMapper {

    public MemberTicketResponse toResponse(MemberTicket mt) {
        return new MemberTicketResponse(
                mt.getId(),
                mt.getMember().getId(),
                mt.getMember().getUser().getId(),
                mt.getMember().getUser().getName(),
                mt.getMember().getRole(),
                mt.getTicket().getId(),
                mt.getCreatedAt()
        );
    }
}
