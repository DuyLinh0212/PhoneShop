package com.example.webbanphone.dto.product;

public record BrandResponse(
        Integer id,
        String name,
        String logo,
        Boolean isActive
) {
}
