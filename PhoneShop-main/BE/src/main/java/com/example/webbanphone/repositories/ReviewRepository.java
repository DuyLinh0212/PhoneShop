package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductIdAndIsApprovedTrueOrderByIdDesc(Integer productId);

    long countByIsApprovedTrue();
}
