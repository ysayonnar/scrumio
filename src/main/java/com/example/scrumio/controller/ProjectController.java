package com.example.scrumio.controller;

import com.example.scrumio.auth.AuthContext;
import com.example.scrumio.auth.RequireAuth;
import com.example.scrumio.service.ProjectService;
import com.example.scrumio.web.dto.ProjectPatchRequest;
import com.example.scrumio.web.dto.ProjectRequest;
import com.example.scrumio.web.dto.ProjectResponse;
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

    @RequireAuth
    @GetMapping
    public List<ProjectResponse> getAll() {
        return service.getAll(AuthContext.getUserId());
    }

    @RequireAuth
    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable UUID id) {
        return service.getById(id, AuthContext.getUserId());
    }

    @RequireAuth
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@RequestBody @Valid ProjectRequest request) {
        return service.create(request, AuthContext.getUserId());
    }

    @RequireAuth
    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable UUID id, @RequestBody @Valid ProjectRequest request) {
        return service.update(id, request, AuthContext.getUserId());
    }

    @RequireAuth
    @PatchMapping("/{id}")
    public ProjectResponse patch(@PathVariable UUID id, @RequestBody ProjectPatchRequest request) {
        return service.patch(id, request, AuthContext.getUserId());
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public ProjectResponse delete(@PathVariable UUID id) {
        return service.delete(id, AuthContext.getUserId());
    }
}
