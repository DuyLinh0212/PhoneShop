package com.example.webbanphone.dto.order;

public record UpdateOrderStatusRequest(String status, String paymentStatus, String note) {
}
