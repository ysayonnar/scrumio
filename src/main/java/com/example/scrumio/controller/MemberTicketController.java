package com.example.scrumio.controller;

import com.example.scrumio.auth.AuthContext;
import com.example.scrumio.auth.RequireAuth;
import com.example.scrumio.service.MemberTicketService;
import com.example.scrumio.web.dto.MemberTicketRequest;
import com.example.scrumio.web.dto.MemberTicketResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/members")
public class MemberTicketController {

    private final MemberTicketService service;

    public MemberTicketController(MemberTicketService service) {
        this.service = service;
    }

    @RequireAuth
    @GetMapping
    public List<MemberTicketResponse> getAll(@PathVariable UUID ticketId) {
        return service.getAll(ticketId, AuthContext.getUserId());
    }

    @RequireAuth
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberTicketResponse assign(@PathVariable UUID ticketId,
                                       @RequestBody @Valid MemberTicketRequest request) {
        return service.assign(ticketId, request, AuthContext.getUserId());
    }

    @RequireAuth
    @DeleteMapping("/{assignmentId}")
    public MemberTicketResponse unassign(@PathVariable UUID ticketId,
                                         @PathVariable UUID assignmentId) {
        return service.unassign(ticketId, assignmentId, AuthContext.getUserId());
    }
}
