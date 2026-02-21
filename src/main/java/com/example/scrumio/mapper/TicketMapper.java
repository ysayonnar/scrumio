package com.example.scrumio.mapper;

import com.example.scrumio.dto.TicketRequest;
import com.example.scrumio.dto.TicketResponse;
import com.example.scrumio.entity.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {
    public Ticket toEntity(TicketRequest request){
        return new Ticket(
                request.title(),
                request.description(),
                request.priority(),
                request.status(),
                request.estimation(),
                request.sprintID()
        );
    }

    public TicketResponse toResponse(Ticket ticket){
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getEstimation(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getDeletedAt(),
                ticket.getSprintID()
        );
    }
}
