package com.example.scrumio.controller;

import com.example.scrumio.dto.TicketRequest;
import com.example.scrumio.dto.TicketResponse;
import com.example.scrumio.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// TODO: add finding by status
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {
    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping
    public List<TicketResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public TicketResponse getByID(@PathVariable UUID id) {
        return service.getByID(id);
    }

    @PostMapping
    public TicketResponse create(@RequestBody TicketRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public TicketResponse update(@PathVariable UUID id, @RequestBody TicketRequest request) {
        // TODO: new dto for updating and so on
        return service.create(request);
    }

    @DeleteMapping("/{id}")
    public TicketResponse delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}