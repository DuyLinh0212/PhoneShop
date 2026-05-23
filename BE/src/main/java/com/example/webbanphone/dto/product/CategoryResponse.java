package com.example.webbanphone.dto.product;

public record CategoryResponse(
        Integer id,
        Integer parentId,
        String name,
        String slug,
        String description,
        Boolean isActive
) {
}
