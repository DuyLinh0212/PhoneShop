package com.example.webbanphone.controllers;

import com.example.webbanphone.dto.product.ReviewRequest;
import com.example.webbanphone.dto.product.ReviewResponse;
import com.example.webbanphone.dto.product.ReviewSummaryResponse;
import com.example.webbanphone.services.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<ReviewSummaryResponse> getReviews(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Integer productId,
            @RequestBody ReviewRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(productId, request, authentication == null ? null : authentication.getName()));
    }
}
