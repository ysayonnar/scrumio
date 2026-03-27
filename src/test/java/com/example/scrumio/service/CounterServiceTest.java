package com.example.scrumio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CounterServiceTest {

    private CounterService service;

    @BeforeEach
    void setUp() {
        service = new CounterService();
    }

    @Test
    void shouldIncrementSafeCounter() {
        assertEquals(1L, service.incrementSafe());
        assertEquals(2L, service.incrementSafe());
    }

    @Test
    void shouldReturnSafeCount() {
        service.incrementSafe();
        service.incrementSafe();
        assertEquals(2L, service.getSafeCount());
    }

    @Test
    void shouldIncrementUnsafeCounter() {
        assertEquals(1L, service.incrementUnsafe());
        assertEquals(2L, service.incrementUnsafe());
    }

    @Test
    void shouldReturnUnsafeCount() {
        service.incrementUnsafe();
        assertEquals(1L, service.getUnsafeCount());
    }

    @Test
    void shouldResetBothCounters() {
        service.incrementSafe();
        service.incrementUnsafe();

        service.reset();

        assertEquals(0L, service.getSafeCount());
        assertEquals(0L, service.getUnsafeCount());
    }
}
