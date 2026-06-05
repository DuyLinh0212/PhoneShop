package com.example.webbanphone.dto.auth;

public record RegisterRequest(
        String fullName,
        String email,
        String password,
        String phone
) {
}
