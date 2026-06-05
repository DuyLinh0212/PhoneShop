package com.example.webbanphone.dto.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<CartItemResponse> items, Integer totalQuantity, BigDecimal subtotal) {
}
