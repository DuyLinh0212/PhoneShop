package com.example.webbanphone.dto.product;

import java.time.LocalDateTime;

public record ReviewResponse(
        Integer id,
        Integer productId,
        Integer userId,
        String userName,
        Integer rating,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
