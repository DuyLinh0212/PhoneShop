package com.example.webbanphone.services;

import com.example.webbanphone.dto.product.ReviewRequest;
import com.example.webbanphone.dto.product.ReviewResponse;
import com.example.webbanphone.dto.product.ReviewSummaryResponse;
import com.example.webbanphone.entities.Review;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.repositories.ProductRepository;
import com.example.webbanphone.repositories.ReviewRepository;
import com.example.webbanphone.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService
    ) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    public ReviewSummaryResponse getProductReviews(Integer productId) {
        ensureProductExists(productId);
        List<ReviewResponse> reviews = reviewRepository.findByProductIdAndIsApprovedTrueOrderByIdDesc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
        double average = reviews.stream()
                .mapToInt(ReviewResponse::rating)
                .average()
                .orElse(0);
        return new ReviewSummaryResponse(Math.round(average * 10.0) / 10.0, reviews.size(), reviews);
    }

    @Transactional
    public ReviewResponse createReview(Integer productId, ReviewRequest request, String email) {
        ensureProductExists(productId);
        if (request == null || request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        User user = currentUserService.getByEmail(email);
        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(user.getId());
        review.setRating(request.rating());
        review.setTitle(trimToNull(request.title()));
        review.setContent(trimToNull(request.content()));
        review.setIsApproved(true);
        review.setCreatedAt(LocalDateTime.now());
        return toResponse(reviewRepository.save(review));
    }

    private void ensureProductExists(Integer productId) {
        if (productId == null || !productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }

    private ReviewResponse toResponse(Review review) {
        String userName = userRepository.findById(review.getUserId())
                .map(User::getFullName)
                .orElse("Khach hang");
        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                userName,
                review.getRating(),
                review.getTitle(),
                review.getContent(),
                review.getCreatedAt()
        );
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
