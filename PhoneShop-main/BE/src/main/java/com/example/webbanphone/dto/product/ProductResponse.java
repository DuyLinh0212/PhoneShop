package com.example.webbanphone.dto.product;

import java.math.BigDecimal;

public record ProductResponse(
        Integer id,
        Integer brandId,
        Integer categoryId,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        String thumbnail,
        Boolean isActive,
        Boolean isFeatured,
        Integer viewCount,
        Integer totalStock
) {
}
