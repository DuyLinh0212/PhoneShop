package com.example.webbanphone.dto.order;

public record CheckoutRequest(
        String shippingName,
        String shippingPhone,
        String shippingAddress,
        String paymentMethod,
        String note
) {
}
