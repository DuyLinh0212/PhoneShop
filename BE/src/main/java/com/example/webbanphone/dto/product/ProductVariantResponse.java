package com.example.webbanphone.dto.product;

import java.math.BigDecimal;

public record ProductVariantResponse(
        Integer id,
        String color,
        String storage,
        String ram,
        BigDecimal price,
        BigDecimal salePrice,
        Integer stock,
        String sku,
        Boolean isActive
) {
}
