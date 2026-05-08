package com.example.webbanphone.dto.brand;

public record BrandResponse(
        Integer id,
        String name,
        String slug,
        String logo,
        Boolean isActive
) {}
