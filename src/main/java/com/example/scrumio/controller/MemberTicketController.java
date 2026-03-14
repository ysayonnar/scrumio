package com.example.scrumio.controller;

import com.example.scrumio.auth.AuthContext;
import com.example.scrumio.auth.RequireAuth;
import com.example.scrumio.service.MemberTicketService;
import com.example.scrumio.web.dto.MemberTicketRequest;
import com.example.scrumio.web.dto.MemberTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Ticket Assignments")
@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/members")
public class MemberTicketController {

    private final MemberTicketService service;

    public MemberTicketController(MemberTicketService service) {
        this.service = service;
    }

    @Operation(summary = "Get all members assigned to a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @RequireAuth
    @GetMapping
    public List<MemberTicketResponse> getAll(@PathVariable UUID ticketId) {
        return service.getAll(ticketId, AuthContext.getUserId());
    }

    @Operation(summary = "Assign a member to a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member assigned"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Ticket or member not found")
    })
    @RequireAuth
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberTicketResponse assign(@PathVariable UUID ticketId,
                                       @RequestBody @Valid MemberTicketRequest request) {
        return service.assign(ticketId, request, AuthContext.getUserId());
    }

    @Operation(summary = "Unassign a member from a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member unassigned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @RequireAuth
    @DeleteMapping("/{assignmentId}")
    public MemberTicketResponse unassign(@PathVariable UUID ticketId,
                                         @PathVariable UUID assignmentId) {
        return service.unassign(ticketId, assignmentId, AuthContext.getUserId());
    }
}
