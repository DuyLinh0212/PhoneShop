package com.example.webbanphone.controllers;

import com.example.webbanphone.dto.order.AdminOrderResponse;
import com.example.webbanphone.dto.order.UpdateOrderStatusRequest;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.services.CurrentUserService;
import com.example.webbanphone.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public AdminOrderController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<AdminOrderResponse>> getOrders(
            @RequestParam(name = "status", required = false) String status
    ) {
        return ResponseEntity.ok(orderService.getAdminOrders(status));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<AdminOrderResponse> updateStatus(
            Authentication authentication,
            @PathVariable Integer orderId,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request, currentUser(authentication)));
    }

    private User currentUser(Authentication authentication) {
        return currentUserService.getByEmail(authentication == null ? null : authentication.getName());
    }
}
