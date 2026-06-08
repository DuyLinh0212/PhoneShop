package com.example.webbanphone.dto.product;

public record ProductImageRequest(
        String imageUrl,
        String altText,
        Integer sortOrder,
        Boolean isMain
) {
}
