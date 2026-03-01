package com.example.scrumio.mapper;

import com.example.scrumio.entity.ticket.Ticket;
import com.example.scrumio.web.dto.TicketResponse;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getEstimation(),
                ticket.getSprint() != null ? ticket.getSprint().getId() : null,
                ticket.getProject().getId(),
                ticket.getCreatedAt()
        );
    }
}
