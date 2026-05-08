package com.example.webbanphone.dto.category;

public record CategoryResponse(
        Integer id,
        String name,
        String slug,
        String description,
        Boolean isActive
) {}
