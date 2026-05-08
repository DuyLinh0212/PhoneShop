package com.example.webbanphone.dto.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Integer userId,
        String fullName,
        String email,
        String role
) {
}
