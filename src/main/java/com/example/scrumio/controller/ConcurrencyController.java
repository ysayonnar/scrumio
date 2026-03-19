package com.example.scrumio.controller;

import com.example.scrumio.service.ConcurrencyService;
import com.example.scrumio.web.dto.CounterResponse;
import com.example.scrumio.web.dto.RaceConditionResponse;
import com.example.scrumio.web.dto.TaskResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/concurrency")
public class ConcurrencyController {

    private final ConcurrencyService concurrencyService;

    public ConcurrencyController(ConcurrencyService concurrencyService) {
        this.concurrencyService = concurrencyService;
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse startTask(@RequestParam(defaultValue = "default-payload") String payload) {
        UUID taskId = concurrencyService.startTask(payload);
        return concurrencyService.getTask(taskId);
    }

    @GetMapping("/tasks/{id}")
    public TaskResponse getTask(@PathVariable UUID id) {
        return concurrencyService.getTask(id);
    }

    @PostMapping("/counter/increment")
    public CounterResponse incrementCounter() {
        return concurrencyService.incrementCounter();
    }

    @GetMapping("/counter")
    public CounterResponse getCounter() {
        return concurrencyService.getCounter();
    }

    @PostMapping("/race")
    public RaceConditionResponse runRace() throws InterruptedException {
        return concurrencyService.runRaceCondition();
    }
}
