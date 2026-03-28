package com.example.scrumio.service;

import com.example.scrumio.concurrency.TaskStatus;
import com.example.scrumio.concurrency.TaskStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncTaskService {

    private final TaskStore taskStore;

    public AsyncTaskService(TaskStore taskStore) {
        this.taskStore = taskStore;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> executeTask(UUID taskId, String payload) {
        taskStore.find(taskId).ifPresent(state -> {
            state.setStatus(TaskStatus.RUNNING);
            try {
                Thread.sleep(15000);
                state.setResult("Processed payload='" + payload + "' at " + OffsetDateTime.now());
                state.setStatus(TaskStatus.COMPLETED);
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                state.setStatus(TaskStatus.FAILED);
                state.setResult("Task was interrupted");
            } catch (Exception e) {
                state.setStatus(TaskStatus.FAILED);
                state.setResult("Error: " + e.getMessage());
            }
        });
        return CompletableFuture.completedFuture(null);
    }
}
