package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductIdAndIsApprovedTrueOrderByIdDesc(Integer productId);

    long countByIsApprovedTrue();

    long countByProductIdAndIsApprovedTrue(Integer productId);

    @Query("select coalesce(avg(r.rating), 0.0) from Review r where r.productId = :productId and r.isApproved = true")
    Double averageRatingByProductId(@Param("productId") Integer productId);
}
