package com.example.webbanphone.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderResponse(
        Integer id,
        Integer userId,
        String customerName,
        String customerEmail,
        String customerPhone,
        String shippingName,
        String shippingPhone,
        String shippingAddress,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        String status,
        String paymentMethod,
        String paymentStatus,
        String note,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
