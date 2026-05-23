package com.example.webbanphone.dto.product;

public record ProductSpecRequest(
        String specKey,
        String specValue,
        Integer sortOrder
) {
}
