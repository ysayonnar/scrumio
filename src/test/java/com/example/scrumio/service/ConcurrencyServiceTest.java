package com.example.scrumio.service;

import com.example.scrumio.concurrency.TaskState;
import com.example.scrumio.concurrency.TaskStatus;
import com.example.scrumio.concurrency.TaskStore;
import com.example.scrumio.web.dto.CounterResponse;
import com.example.scrumio.web.dto.RaceConditionResponse;
import com.example.scrumio.web.dto.TaskResponse;
import com.example.scrumio.web.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcurrencyServiceTest {

    @Mock
    private TaskStore taskStore;
    @Mock
    private AsyncTaskService asyncTaskService;
    @Mock
    private CounterService counterService;

    @InjectMocks
    private ConcurrencyService service;

    @Nested
    class StartTask {

        @Test
        void shouldRegisterStateAndTriggerAsync() {
            UUID taskId = UUID.randomUUID();

            when(taskStore.register(any(TaskState.class))).thenReturn(taskId);

            UUID result = service.startTask("my-payload");

            assertEquals(taskId, result);
            verify(asyncTaskService).executeTask(taskId, "my-payload");
        }
    }

    @Nested
    class GetTask {

        @Test
        void shouldReturnTaskResponse() {
            UUID taskId = UUID.randomUUID();
            TaskState state = new TaskState();
            state.setStatus(TaskStatus.RUNNING);
            state.setResult(null);

            when(taskStore.find(taskId)).thenReturn(Optional.of(state));

            TaskResponse response = service.getTask(taskId);

            assertEquals(taskId, response.taskId());
            assertEquals(TaskStatus.RUNNING, response.status());
        }

        @Test
        void shouldThrowWhenTaskNotFound() {
            UUID taskId = UUID.randomUUID();
            when(taskStore.find(taskId)).thenReturn(Optional.empty());

            assertThrows(TaskNotFoundException.class, () -> service.getTask(taskId));
        }
    }

    @Nested
    class IncrementCounter {

        @Test
        void shouldDelegateToBothCounters() {
            when(counterService.incrementSafe()).thenReturn(5L);
            when(counterService.incrementUnsafe()).thenReturn(4L);

            CounterResponse response = service.incrementCounter();

            assertEquals(5L, response.safeCount());
            assertEquals(4L, response.unsafeCount());
        }
    }

    @Nested
    class GetCounter {

        @Test
        void shouldReturnCurrentCounts() {
            when(counterService.getSafeCount()).thenReturn(10L);
            when(counterService.getUnsafeCount()).thenReturn(9L);

            CounterResponse response = service.getCounter();

            assertEquals(10L, response.safeCount());
            assertEquals(9L, response.unsafeCount());
        }
    }

    @Nested
    class RunRaceCondition {

        @Test
        void shouldShowSafeCounterIsAlwaysCorrect() throws InterruptedException {
            ConcurrencyService realService = new ConcurrencyService(taskStore, asyncTaskService, counterService);

            RaceConditionResponse response = realService.runRaceCondition();

            assertEquals(50, response.threadCount());
            assertEquals(1000, response.incrementsPerThread());
            assertEquals(50_000L, response.expected());
            assertEquals(50_000L, response.safeResult());
            assertTrue(response.safeCorrect());
            assertNotNull(response.unsafeResult());
        }

        @Test
        void shouldDemonstrateRaceConditionIsLikelyToOccur() throws InterruptedException {
            ConcurrencyService realService = new ConcurrencyService(taskStore, asyncTaskService, counterService);

            boolean raceOccurredAtLeastOnce = false;
            for (int attempt = 0; attempt < 5; attempt++) {
                RaceConditionResponse response = realService.runRaceCondition();
                if (response.raceConditionOccurred()) {
                    raceOccurredAtLeastOnce = true;
                    break;
                }
            }
            assertTrue(raceOccurredAtLeastOnce, "Race condition should occur at least once in 5 attempts with 50 threads");
        }

        @Test
        void safeCounterShouldNeverLoseIncrements() throws InterruptedException {
            ConcurrencyService realService = new ConcurrencyService(taskStore, asyncTaskService, counterService);

            for (int i = 0; i < 3; i++) {
                RaceConditionResponse response = realService.runRaceCondition();
                assertTrue(response.safeCorrect(), "AtomicLong must always produce correct result");
                assertFalse(response.safeResult() != response.expected(),
                        "Safe counter must equal expected after each run");
            }
        }
    }
}
