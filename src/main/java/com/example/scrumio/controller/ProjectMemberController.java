package com.example.scrumio.controller;

import com.example.scrumio.auth.AuthContext;
import com.example.scrumio.auth.RequireAuth;
import com.example.scrumio.service.ProjectMemberService;
import com.example.scrumio.web.dto.ProjectMemberRequest;
import com.example.scrumio.web.dto.ProjectMemberResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Project Members")
@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService service;

    public ProjectMemberController(ProjectMemberService service) {
        this.service = service;
    }

    @Operation(summary = "Get all members of a project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @RequireAuth
    @GetMapping
    public List<ProjectMemberResponse> getAll(@PathVariable UUID projectId) {
        return service.getAll(projectId, AuthContext.getUserId());
    }

    @Operation(summary = "Add a member to a project (OWNER or MANAGER only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member added"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or user not found")
    })
    @RequireAuth
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse addMember(@PathVariable UUID projectId,
                                           @RequestBody @Valid ProjectMemberRequest request) {
        return service.addMember(projectId, request, AuthContext.getUserId());
    }

    @Operation(summary = "Update a member's role (OWNER or MANAGER only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @RequireAuth
    @PatchMapping("/{memberId}")
    public ProjectMemberResponse updateRole(@PathVariable UUID projectId,
                                            @PathVariable UUID memberId,
                                            @RequestBody @Valid ProjectMemberRequest request) {
        return service.updateRole(projectId, memberId, request, AuthContext.getUserId());
    }

    @Operation(summary = "Remove a member from a project (OWNER only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member removed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @RequireAuth
    @DeleteMapping("/{memberId}")
    public ProjectMemberResponse removeMember(@PathVariable UUID projectId,
                                              @PathVariable UUID memberId) {
        return service.removeMember(projectId, memberId, AuthContext.getUserId());
    }
}
