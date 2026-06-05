package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.ProductSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSpecRepository extends JpaRepository<ProductSpec, Integer> {
    List<ProductSpec> findByProductIdOrderBySortOrderAscIdAsc(Integer productId);

    void deleteByProductId(Integer productId);
}
