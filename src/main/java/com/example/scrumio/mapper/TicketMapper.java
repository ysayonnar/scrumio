package com.example.scrumio.mapper;

import com.example.scrumio.entity.ticket.Ticket;
import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import com.example.scrumio.web.dto.TicketNativeProjection;
import com.example.scrumio.web.dto.TicketResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class TicketMapper {

    public TicketResponse fromNativeProjection(TicketNativeProjection p) {
        return new TicketResponse(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getPriority() != null ? TicketPriority.valueOf(p.getPriority()) : null,
                TicketStatus.valueOf(p.getStatus()),
                p.getEstimation(),
                p.getSprintId(),
                p.getSprintName(),
                p.getProjectId(),
                p.getCreatedAt().atOffset(ZoneOffset.UTC)
        );
    }

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getEstimation(),
                ticket.getSprint() != null ? ticket.getSprint().getId() : null,
                ticket.getSprint() != null ? ticket.getSprint().getName() : null,
                ticket.getProject().getId(),
                ticket.getCreatedAt()
        );
    }
}
