package com.example.webbanphone.dto.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        Integer brandId,
        Integer categoryId,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        String thumbnail,
        Boolean isActive,
        Boolean isFeatured,
        List<ProductVariantRequest> variants,
        List<ProductImageRequest> images,
        List<ProductSpecRequest> specs
) {
}
