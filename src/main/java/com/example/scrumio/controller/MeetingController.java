package com.example.scrumio.controller;

import com.example.scrumio.auth.AuthContext;
import com.example.scrumio.auth.RequireAuth;
import com.example.scrumio.service.MeetingService;
import com.example.scrumio.web.dto.MeetingPatchRequest;
import com.example.scrumio.web.dto.MeetingRequest;
import com.example.scrumio.web.dto.MeetingResponse;
import com.example.scrumio.web.dto.MeetingWithMembersRequest;
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

@Tag(name = "Meetings")
@RestController
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingService service;

    public MeetingController(MeetingService service) {
        this.service = service;
    }

    @Operation(summary = "Get all meetings for a project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Missing project_id parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @GetMapping
    public List<MeetingResponse> getAll(@RequestParam("project_id") UUID projectId) {
        return service.getAll(projectId, AuthContext.getUserId());
    }

    @Operation(summary = "Get meeting by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    @RequireAuth
    @GetMapping("/{id}")
    public MeetingResponse getById(@PathVariable UUID id) {
        return service.getById(id, AuthContext.getUserId());
    }

    @Operation(summary = "Create a new meeting")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meeting created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse create(@RequestBody @Valid MeetingRequest request) {
        return service.create(request, AuthContext.getUserId());
    }

    @Operation(summary = "Replace a meeting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    @RequireAuth
    @PutMapping("/{id}")
    public MeetingResponse update(@PathVariable UUID id, @RequestBody @Valid MeetingRequest request) {
        return service.update(id, request, AuthContext.getUserId());
    }

    @Operation(summary = "Partially update a meeting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    @RequireAuth
    @PatchMapping("/{id}")
    public MeetingResponse patch(@PathVariable UUID id, @RequestBody MeetingPatchRequest request) {
        return service.patch(id, request, AuthContext.getUserId());
    }

    @Operation(summary = "Delete a meeting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meeting deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    @RequireAuth
    @DeleteMapping("/{id}")
    public MeetingResponse delete(@PathVariable UUID id) {
        return service.delete(id, AuthContext.getUserId());
    }

    @Operation(summary = "Create a meeting with members (transactional, batch load)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meeting created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or member not found")
    })
    @RequireAuth
    @PostMapping("/with-members")
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse createWithMembers(@RequestBody @Valid MeetingWithMembersRequest request) {
        return service.createWithMembers(request, AuthContext.getUserId());
    }

    @Operation(summary = "Create a meeting with members (N+1 demo, non-transactional)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meeting created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or member not found")
    })
    @RequireAuth
    @PostMapping("/with-members-unsafe")
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse createWithMembersUnsafe(@RequestBody @Valid MeetingWithMembersRequest request) {
        return service.createWithMembersUnsafe(request, AuthContext.getUserId());
    }
}
