package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserIdOrderByIdDesc(Integer userId);

    Optional<CartItem> findByUserIdAndVariantId(Integer userId, Integer variantId);

    void deleteByUserIdAndId(Integer userId, Integer id);

    void deleteByUserId(Integer userId);

    long countByUserId(Integer userId);
}
