package com.example.webbanphone.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Integer userId,
        String fullName,
        String email,
        String role
) {
}
