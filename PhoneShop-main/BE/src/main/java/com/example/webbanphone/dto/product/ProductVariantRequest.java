package com.example.webbanphone.dto.product;

import java.math.BigDecimal;

public record ProductVariantRequest(
        String color,
        String storage,
        String ram,
        BigDecimal price,
        BigDecimal salePrice,
        BigDecimal costPrice,
        BigDecimal discountPercent,
        Integer stock,
        String sku,
        Boolean isActive
) {
}
