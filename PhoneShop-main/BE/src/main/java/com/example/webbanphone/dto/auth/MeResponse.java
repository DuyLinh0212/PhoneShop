package com.example.webbanphone.dto.auth;

public record MeResponse(
        Integer userId,
        String fullName,
        String email,
        String role
) {
}
