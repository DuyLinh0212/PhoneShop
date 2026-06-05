package com.example.webbanphone.controllers;

import com.example.webbanphone.dto.cart.AddCartItemRequest;
import com.example.webbanphone.dto.cart.CartResponse;
import com.example.webbanphone.dto.cart.UpdateCartItemRequest;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.services.CartService;
import com.example.webbanphone.services.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public CartController(CartService cartService, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(currentUser(authentication)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(currentUser(authentication), request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            Authentication authentication,
            @PathVariable Integer itemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItem(currentUser(authentication), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(Authentication authentication, @PathVariable Integer itemId) {
        return ResponseEntity.ok(cartService.removeItem(currentUser(authentication), itemId));
    }

    private User currentUser(Authentication authentication) {
        return currentUserService.getByEmail(authentication == null ? null : authentication.getName());
    }
}
