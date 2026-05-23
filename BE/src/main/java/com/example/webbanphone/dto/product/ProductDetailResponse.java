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
        String thumbnail,
        Boolean isActive,
        Boolean isFeatured,
        Integer viewCount,
        List<ProductVariantResponse> variants,
        List<ProductImageResponse> images,
        List<ProductSpecResponse> specs
) {
}
