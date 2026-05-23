package com.example.webbanphone.dto.product;

public record ProductSpecResponse(
        Integer id,
        String specKey,
        String specValue,
        Integer sortOrder
) {
}
