package com.example.webbanphone.controllers;

import com.example.webbanphone.dto.order.CheckoutRequest;
import com.example.webbanphone.dto.order.OrderResponse;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.services.CurrentUserService;
import com.example.webbanphone.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public OrderController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrders(currentUser(authentication)));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            Authentication authentication,
            @RequestBody CheckoutRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(currentUser(authentication), request));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(Authentication authentication, @PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(currentUser(authentication), orderId));
    }

    private User currentUser(Authentication authentication) {
        return currentUserService.getByEmail(authentication == null ? null : authentication.getName());
    }
}
