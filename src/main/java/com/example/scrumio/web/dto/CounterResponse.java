package com.example.scrumio.web.dto;

public record CounterResponse(
        long safeCount,
        long unsafeCount
) {
}
