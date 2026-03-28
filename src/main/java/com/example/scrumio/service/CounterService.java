package com.example.scrumio.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class CounterService {

    private final AtomicLong safeCounter = new AtomicLong(0);
    private long unsafeCounter = 0;

    public long incrementSafe() {
        return safeCounter.incrementAndGet();
    }

    public long getSafeCount() {
        return safeCounter.get();
    }

    public long incrementUnsafe() {
        return ++unsafeCounter;
    }

    public long getUnsafeCount() {
        return unsafeCounter;
    }

    public void reset() {
        safeCounter.set(0);
        unsafeCounter = 0;
    }
}
