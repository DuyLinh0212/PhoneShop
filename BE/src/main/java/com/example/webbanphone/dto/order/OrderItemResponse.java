package com.example.webbanphone.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Integer id,
        Integer variantId,
        String productName,
        String variantInfo,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
