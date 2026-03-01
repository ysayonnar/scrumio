package com.example.scrumio.controller;

import com.example.scrumio.service.ProjectService;
import com.example.scrumio.web.dto.ProjectRequest;
import com.example.scrumio.web.dto.ProjectResponse;
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
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProjectResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@RequestBody @Valid ProjectRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable UUID id, @RequestBody @Valid ProjectRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ProjectResponse delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
