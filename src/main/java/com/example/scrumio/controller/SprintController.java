package com.example.scrumio.controller;

import com.example.scrumio.service.SprintService;
import com.example.scrumio.web.dto.SprintRequest;
import com.example.scrumio.web.dto.SprintResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sprints")
public class SprintController {

    private final SprintService service;

    public SprintController(SprintService service) {
        this.service = service;
    }

    @GetMapping
    public List<SprintResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public SprintResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SprintResponse create(@RequestBody @Valid SprintRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public SprintResponse update(@PathVariable UUID id, @RequestBody @Valid SprintRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public SprintResponse delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
