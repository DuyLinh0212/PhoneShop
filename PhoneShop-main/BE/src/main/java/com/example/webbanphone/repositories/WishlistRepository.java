package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByUserIdOrderByIdDesc(Integer userId);

    Optional<Wishlist> findByUserIdAndProductId(Integer userId, Integer productId);

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

    long countByUserId(Integer userId);

    void deleteByUserIdAndProductId(Integer userId, Integer productId);
}
