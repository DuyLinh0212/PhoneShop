package com.example.webbanphone.dto.auth;

public record RegisterResponse(
        Integer userId,
        String fullName,
        String email,
        String role
) {
}
