package com.example.scrumio.controller;

import com.example.scrumio.service.TicketService;
import com.example.scrumio.web.dto.TicketRequest;
import com.example.scrumio.web.dto.TicketResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public List<TicketResponse> getAll(@RequestParam(required = false) String status,
                                       @RequestParam(required = false) String priority) {
        return service.getAll(status, priority);
    }

    @GetMapping("/{id}")
    public TicketResponse getByID(@PathVariable UUID id) {
        return service.getByID(id);
    }

    @PostMapping
    public TicketResponse create(@RequestBody @Valid TicketRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public TicketResponse update(@PathVariable UUID id, @RequestBody @Valid TicketRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public TicketResponse delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}