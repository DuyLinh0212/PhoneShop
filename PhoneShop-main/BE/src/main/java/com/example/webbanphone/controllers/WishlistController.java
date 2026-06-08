package com.example.webbanphone.controllers;

import com.example.webbanphone.dto.product.ProductDetailResponse;
import com.example.webbanphone.dto.wishlist.FavoriteStatusResponse;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.services.CurrentUserService;
import com.example.webbanphone.services.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final CurrentUserService currentUserService;

    public WishlistController(WishlistService wishlistService, CurrentUserService currentUserService) {
        this.wishlistService = wishlistService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDetailResponse>> getFavorites(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.getFavorites(currentUser(authentication)));
    }

    @GetMapping("/{productId}/status")
    public ResponseEntity<FavoriteStatusResponse> getStatus(
            Authentication authentication,
            @PathVariable Integer productId
    ) {
        return ResponseEntity.ok(wishlistService.getStatus(currentUser(authentication), productId));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<FavoriteStatusResponse> addFavorite(
            Authentication authentication,
            @PathVariable Integer productId
    ) {
        return ResponseEntity.ok(wishlistService.addFavorite(currentUser(authentication), productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<FavoriteStatusResponse> removeFavorite(
            Authentication authentication,
            @PathVariable Integer productId
    ) {
        return ResponseEntity.ok(wishlistService.removeFavorite(currentUser(authentication), productId));
    }

    private User currentUser(Authentication authentication) {
        return currentUserService.getByEmail(authentication == null ? null : authentication.getName());
    }
}
