package com.example.scrumio.concurrency;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TaskStore {

    private final ConcurrentHashMap<UUID, TaskState> store = new ConcurrentHashMap<>();

    public UUID register(TaskState state) {
        UUID id = UUID.randomUUID();
        store.put(id, state);
        return id;
    }

    public Optional<TaskState> find(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
