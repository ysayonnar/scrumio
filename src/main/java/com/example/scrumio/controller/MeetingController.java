package com.example.scrumio.controller;

import com.example.scrumio.auth.AuthContext;
import com.example.scrumio.auth.RequireAuth;
import com.example.scrumio.service.MeetingService;
import com.example.scrumio.web.dto.MeetingPatchRequest;
import com.example.scrumio.web.dto.MeetingRequest;
import com.example.scrumio.web.dto.MeetingResponse;
import com.example.scrumio.web.dto.MeetingWithMembersRequest;
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
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingService service;

    public MeetingController(MeetingService service) {
        this.service = service;
    }

    @RequireAuth
    @GetMapping
    public List<MeetingResponse> getAll(@RequestParam UUID projectId) {
        return service.getAll(projectId, AuthContext.getUserId());
    }

    @RequireAuth
    @GetMapping("/{id}")
    public MeetingResponse getById(@PathVariable UUID id) {
        return service.getById(id, AuthContext.getUserId());
    }

    @RequireAuth
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse create(@RequestBody @Valid MeetingRequest request) {
        return service.create(request, AuthContext.getUserId());
    }

    @RequireAuth
    @PutMapping("/{id}")
    public MeetingResponse update(@PathVariable UUID id, @RequestBody @Valid MeetingRequest request) {
        return service.update(id, request, AuthContext.getUserId());
    }

    @RequireAuth
    @PatchMapping("/{id}")
    public MeetingResponse patch(@PathVariable UUID id, @RequestBody MeetingPatchRequest request) {
        return service.patch(id, request, AuthContext.getUserId());
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public MeetingResponse delete(@PathVariable UUID id) {
        return service.delete(id, AuthContext.getUserId());
    }

    // WITH @Transactional — full rollback if any memberId is invalid
    @RequireAuth
    @PostMapping("/with-members")
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse createWithMembers(@RequestBody @Valid MeetingWithMembersRequest request) {
        return service.createWithMembers(request, AuthContext.getUserId());
    }

    // WITHOUT @Transactional — partial save on error (demo: meeting is committed even if a member lookup fails)
    @RequireAuth
    @PostMapping("/with-members-unsafe")
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse createWithMembersUnsafe(@RequestBody @Valid MeetingWithMembersRequest request) {
        return service.createWithMembersUnsafe(request, AuthContext.getUserId());
    }
}
