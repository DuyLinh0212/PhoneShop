package com.example.webbanphone.dto.product;

public record ProductImageResponse(
        Integer id,
        String imageUrl,
        String altText,
        Integer sortOrder,
        Boolean isMain
) {
}
