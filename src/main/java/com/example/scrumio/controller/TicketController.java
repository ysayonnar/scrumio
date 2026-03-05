package com.example.scrumio.controller;

import com.example.scrumio.auth.AuthContext;
import com.example.scrumio.auth.RequireAuth;
import com.example.scrumio.service.TicketService;
import com.example.scrumio.web.dto.TicketPatchRequest;
import com.example.scrumio.web.dto.TicketRequest;
import com.example.scrumio.web.dto.TicketResponse;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @RequireAuth
    @GetMapping
    public List<TicketResponse> getAll(@RequestParam("project_id") UUID projectId,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String priority) {
        return service.getAll(projectId, AuthContext.getUserId(), status, priority);
    }

    @RequireAuth
    @GetMapping("/safe")
    public List<TicketResponse> getAllSafe(@RequestParam("project_id") UUID projectId) {
        return service.getAllSafe(projectId, AuthContext.getUserId());
    }

    @RequireAuth
    @GetMapping("/unsafe")
    public List<TicketResponse> getAllUnsafe(@RequestParam("project_id") UUID projectId) {
        return service.getAllUnsafe(projectId, AuthContext.getUserId());
    }

    @RequireAuth
    @GetMapping("/{id}")
    public TicketResponse getByID(@PathVariable UUID id) {
        return service.getByID(id, AuthContext.getUserId());
    }

    @RequireAuth
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@RequestBody @Valid TicketRequest request) {
        return service.create(request, AuthContext.getUserId());
    }

    @RequireAuth
    @PutMapping("/{id}")
    public TicketResponse update(@PathVariable UUID id, @RequestBody @Valid TicketRequest request) {
        return service.update(id, request, AuthContext.getUserId());
    }

    @RequireAuth
    @PatchMapping("/{id}")
    public TicketResponse patch(@PathVariable UUID id, @RequestBody TicketPatchRequest request) {
        return service.patch(id, request, AuthContext.getUserId());
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public TicketResponse delete(@PathVariable UUID id) {
        return service.delete(id, AuthContext.getUserId());
    }
}
