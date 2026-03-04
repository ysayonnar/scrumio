package com.example.scrumio.web.dto;

public record UserPatchRequest(
        String name,
        String email,
        String password
) {
}
