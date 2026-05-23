package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductIdOrderByIdAsc(Integer productId);

    void deleteByProductId(Integer productId);
}
