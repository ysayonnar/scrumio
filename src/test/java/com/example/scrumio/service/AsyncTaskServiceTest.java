package com.example.scrumio.service;

import com.example.scrumio.concurrency.TaskState;
import com.example.scrumio.concurrency.TaskStatus;
import com.example.scrumio.concurrency.TaskStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncTaskServiceTest {

    @Mock
    private TaskStore taskStore;

    @InjectMocks
    private AsyncTaskService service;

    @Test
    void shouldDoNothingWhenTaskNotFound() {
        UUID taskId = UUID.randomUUID();
        when(taskStore.find(taskId)).thenReturn(Optional.empty());

        CompletableFuture<Void> result = service.executeTask(taskId, "payload");

        assertNotNull(result);
    }

    @Test
    void shouldMarkTaskAsFailedWhenInterrupted() {
        UUID taskId = UUID.randomUUID();
        TaskState state = new TaskState();
        when(taskStore.find(taskId)).thenReturn(Optional.of(state));

        Thread.currentThread().interrupt();
        service.executeTask(taskId, "payload");
        Thread.interrupted();

        assertEquals(TaskStatus.FAILED, state.getStatus());
        assertEquals("Task was interrupted", state.getResult());
    }

    @Test
    void shouldMarkTaskAsFailedOnException() {
        UUID taskId = UUID.randomUUID();
        TaskState spyState = spy(new TaskState());
        when(taskStore.find(taskId)).thenReturn(Optional.of(spyState));
        lenient().doThrow(new RuntimeException("Simulated failure"))
                .when(spyState).setStatus(TaskStatus.COMPLETED);

        service.executeTask(taskId, "payload");

        assertEquals(TaskStatus.FAILED, spyState.getStatus());
        assertNotNull(spyState.getResult());
    }
}
