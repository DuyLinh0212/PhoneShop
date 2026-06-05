package com.example.webbanphone.dto.product;

import java.util.List;

public record ReviewSummaryResponse(
        double averageRating,
        long totalReviews,
        List<ReviewResponse> reviews
) {
}
