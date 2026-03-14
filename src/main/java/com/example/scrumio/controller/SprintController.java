package com.example.scrumio.controller;

import com.example.scrumio.auth.AuthContext;
import com.example.scrumio.auth.RequireAuth;
import com.example.scrumio.service.SprintService;
import com.example.scrumio.web.dto.SprintPatchRequest;
import com.example.scrumio.web.dto.SprintRequest;
import com.example.scrumio.web.dto.SprintResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Sprints")
@RestController
@RequestMapping("/api/v1/sprints")
public class SprintController {

    private final SprintService service;

    public SprintController(SprintService service) {
        this.service = service;
    }

    @Operation(summary = "Get all sprints for a project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Missing project_id parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @GetMapping
    public List<SprintResponse> getAll(@RequestParam("project_id") UUID projectId) {
        return service.getAll(projectId, AuthContext.getUserId());
    }

    @Operation(summary = "Get sprint by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Sprint not found")
    })
    @RequireAuth
    @GetMapping("/{id}")
    public SprintResponse getById(@PathVariable UUID id) {
        return service.getById(id, AuthContext.getUserId());
    }

    @Operation(summary = "Create a new sprint")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sprint created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SprintResponse create(@RequestBody @Valid SprintRequest request) {
        return service.create(request, AuthContext.getUserId());
    }

    @Operation(summary = "Replace a sprint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Sprint not found")
    })
    @RequireAuth
    @PutMapping("/{id}")
    public SprintResponse update(@PathVariable UUID id, @RequestBody @Valid SprintRequest request) {
        return service.update(id, request, AuthContext.getUserId());
    }

    @Operation(summary = "Partially update a sprint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Sprint not found")
    })
    @RequireAuth
    @PatchMapping("/{id}")
    public SprintResponse patch(@PathVariable UUID id, @RequestBody SprintPatchRequest request) {
        return service.patch(id, request, AuthContext.getUserId());
    }

    @Operation(summary = "Delete a sprint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sprint deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Sprint not found")
    })
    @RequireAuth
    @DeleteMapping("/{id}")
    public SprintResponse delete(@PathVariable UUID id) {
        return service.delete(id, AuthContext.getUserId());
    }
}
