package com.example.scrumio.controller;

import com.example.scrumio.service.MeetingService;
import com.example.scrumio.web.dto.MeetingRequest;
import com.example.scrumio.web.dto.MeetingResponse;
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
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingService service;

    public MeetingController(MeetingService service) {
        this.service = service;
    }

    @GetMapping
    public List<MeetingResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public MeetingResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse create(@RequestBody @Valid MeetingRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public MeetingResponse update(@PathVariable UUID id, @RequestBody @Valid MeetingRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MeetingResponse delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
