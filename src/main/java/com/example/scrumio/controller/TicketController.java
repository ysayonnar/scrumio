package com.example.scrumio.controller;

import com.example.scrumio.auth.AuthContext;
import com.example.scrumio.auth.RequireAuth;
import com.example.scrumio.entity.sprint.SprintStatus;
import com.example.scrumio.entity.ticket.TicketPriority;
import com.example.scrumio.entity.ticket.TicketStatus;
import com.example.scrumio.service.TicketService;
import com.example.scrumio.web.dto.BulkTicketRequest;
import com.example.scrumio.web.dto.TicketPatchRequest;
import com.example.scrumio.web.dto.TicketRequest;
import com.example.scrumio.web.dto.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Tickets")
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @Operation(summary = "Get paginated tickets for a project with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Missing project_id parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @GetMapping
    public Page<TicketResponse> getAll(@RequestParam("project_id") UUID projectId,
                                       @RequestParam(required = false) TicketStatus status,
                                       @RequestParam(required = false) TicketPriority priority,
                                       @RequestParam(name = "sprint_status", required = false) SprintStatus sprintStatus,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return service.getAll(projectId, AuthContext.getUserId(), status, priority, sprintStatus, PageRequest.of(page, size));
    }

    @Operation(summary = "Get paginated tickets using native query")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Missing project_id parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @GetMapping("/native")
    public Page<TicketResponse> getAllNative(@RequestParam("project_id") UUID projectId,
                                             @RequestParam(required = false) TicketStatus status,
                                             @RequestParam(required = false) TicketPriority priority,
                                             @RequestParam(name = "sprint_status", required = false) SprintStatus sprintStatus,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return service.getAllNative(projectId, AuthContext.getUserId(), status, priority, sprintStatus, PageRequest.of(page, size));
    }

    @Operation(summary = "Get all tickets without N+1 (JOIN FETCH demo)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @GetMapping("/safe")
    public List<TicketResponse> getAllSafe(@RequestParam("project_id") UUID projectId) {
        return service.getAllSafe(projectId, AuthContext.getUserId());
    }

    @Operation(summary = "Get all tickets with N+1 queries (demo)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @GetMapping("/unsafe")
    public List<TicketResponse> getAllUnsafe(@RequestParam("project_id") UUID projectId) {
        return service.getAllUnsafe(projectId, AuthContext.getUserId());
    }

    @Operation(summary = "Get ticket by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @RequireAuth
    @GetMapping("/{id}")
    public TicketResponse getByID(@PathVariable UUID id) {
        return service.getByID(id, AuthContext.getUserId());
    }

    @Operation(summary = "Create a new ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@RequestBody @Valid TicketRequest request) {
        return service.create(request, AuthContext.getUserId());
    }

    @Operation(summary = "Replace a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @RequireAuth
    @PutMapping("/{id}")
    public TicketResponse update(@PathVariable UUID id, @RequestBody @Valid TicketRequest request) {
        return service.update(id, request, AuthContext.getUserId());
    }

    @Operation(summary = "Partially update a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @RequireAuth
    @PatchMapping("/{id}")
    public TicketResponse patch(@PathVariable UUID id, @RequestBody TicketPatchRequest request) {
        return service.patch(id, request, AuthContext.getUserId());
    }

    @Operation(summary = "Bulk create tickets with member assignments (transactional, batch load)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tickets created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or member not found")
    })
    @RequireAuth
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<TicketResponse> createBulk(@RequestBody @Valid BulkTicketRequest request) {
        return service.createBulk(request, AuthContext.getUserId());
    }

    @Operation(summary = "Bulk create tickets (N+1 demo, non-transactional)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tickets created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or member not found")
    })
    @RequireAuth
    @PostMapping("/bulk-unsafe")
    @ResponseStatus(HttpStatus.CREATED)
    public List<TicketResponse> createBulkUnsafe(@RequestBody @Valid BulkTicketRequest request) {
        return service.createBulkUnsafe(request, AuthContext.getUserId());
    }

    @Operation(summary = "Delete a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @RequireAuth
    @DeleteMapping("/{id}")
    public TicketResponse delete(@PathVariable UUID id) {
        return service.delete(id, AuthContext.getUserId());
    }
}
