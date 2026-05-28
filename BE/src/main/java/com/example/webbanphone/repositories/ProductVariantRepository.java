package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductIdOrderByIdAsc(Integer productId);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndProductIdNot(String sku, Integer productId);

    void deleteByProductId(Integer productId);
}
