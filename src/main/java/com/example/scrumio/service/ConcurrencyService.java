package com.example.scrumio.service;

import com.example.scrumio.concurrency.TaskState;
import com.example.scrumio.concurrency.TaskStore;
import com.example.scrumio.web.dto.CounterResponse;
import com.example.scrumio.web.dto.RaceConditionResponse;
import com.example.scrumio.web.dto.TaskResponse;
import com.example.scrumio.web.exception.TaskNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ConcurrencyService {

    private static final int RACE_THREAD_COUNT = 50;
    private static final int RACE_INCREMENTS_PER_THREAD = 1000;

    private final TaskStore taskStore;
    private final AsyncTaskService asyncTaskService;
    private final CounterService counterService;

    public ConcurrencyService(TaskStore taskStore, AsyncTaskService asyncTaskService, CounterService counterService) {
        this.taskStore = taskStore;
        this.asyncTaskService = asyncTaskService;
        this.counterService = counterService;
    }

    public UUID startTask(String payload) {
        TaskState state = new TaskState();
        UUID taskId = taskStore.register(state);
        asyncTaskService.executeTask(taskId, payload);
        return taskId;
    }

    public TaskResponse getTask(UUID taskId) {
        TaskState state = taskStore.find(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        return new TaskResponse(taskId, state.getStatus(), state.getResult(), state.getCreatedAt());
    }

    public CounterResponse incrementCounter() {
        long safe = counterService.incrementSafe();
        long unsafe = counterService.incrementUnsafe();
        return new CounterResponse(safe, unsafe);
    }

    public CounterResponse getCounter() {
        return new CounterResponse(counterService.getSafeCount(), counterService.getUnsafeCount());
    }

    public RaceConditionResponse runRaceCondition() throws InterruptedException {
        long expected = (long) RACE_THREAD_COUNT * RACE_INCREMENTS_PER_THREAD;

        long[] unsafeCounter = {0};
        AtomicLong safeCounter = new AtomicLong(0);

        ExecutorService executor = new ThreadPoolExecutor(
                RACE_THREAD_COUNT, RACE_THREAD_COUNT,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(RACE_THREAD_COUNT)
        );
        CountDownLatch latch = new CountDownLatch(RACE_THREAD_COUNT);

        for (int i = 0; i < RACE_THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < RACE_INCREMENTS_PER_THREAD; j++) {
                    unsafeCounter[0]++;
                    safeCounter.incrementAndGet();
                }
                latch.countDown();
            });
        }

        boolean completed = awaitLatch(latch);
        executor.shutdown();
        if (!completed) {
            throw new IllegalStateException("Race condition threads did not finish within timeout");
        }

        long safeResult = safeCounter.get();
        long unsafeResult = unsafeCounter[0];

        return new RaceConditionResponse(
                RACE_THREAD_COUNT,
                RACE_INCREMENTS_PER_THREAD,
                expected,
                safeResult,
                unsafeResult,
                safeResult == expected,
                unsafeResult != expected
        );
    }

    boolean awaitLatch(CountDownLatch latch) throws InterruptedException {
        return latch.await(30, TimeUnit.SECONDS);
    }
}
