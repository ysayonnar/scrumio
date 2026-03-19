package com.example.scrumio.web.dto;

public record RaceConditionResponse(
        int threadCount,
        int incrementsPerThread,
        long expected,
        long safeResult,
        long unsafeResult,
        boolean safeCorrect,
        boolean raceConditionOccurred
) {
}
