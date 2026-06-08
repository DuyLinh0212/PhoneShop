package com.example.webbanphone.dto.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResponse(
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
        double averageRating,
        long reviewCount,
        List<ProductVariantResponse> variants,
        List<ProductImageResponse> images,
        List<ProductSpecResponse> specs
) {
}
