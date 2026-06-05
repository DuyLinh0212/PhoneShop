package com.example.webbanphone.dto.product;

public record ReviewRequest(
        Integer rating,
        String title,
        String content
) {
}
