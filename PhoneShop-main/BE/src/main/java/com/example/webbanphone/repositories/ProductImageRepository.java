package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductIdOrderBySortOrderAscIdAsc(Integer productId);

    void deleteByProductId(Integer productId);
}
