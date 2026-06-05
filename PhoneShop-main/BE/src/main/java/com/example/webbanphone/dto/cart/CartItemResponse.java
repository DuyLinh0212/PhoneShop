package com.example.webbanphone.dto.cart;

import java.math.BigDecimal;

public record CartItemResponse(
        Integer id,
        Integer productId,
        Integer variantId,
        String productName,
        String thumbnail,
        String color,
        String storage,
        String ram,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        Integer stock,
        BigDecimal subtotal
) {
}
