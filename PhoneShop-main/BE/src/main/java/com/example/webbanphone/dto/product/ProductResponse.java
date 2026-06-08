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
        BigDecimal originalPrice,
        BigDecimal salePrice,
        BigDecimal discountPercent,
        String thumbnail,
        Boolean isActive,
        Boolean isFeatured,
        Integer viewCount,
        Integer totalStock,
        double averageRating,
        long reviewCount
) {
}
