package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductIdOrderByIdAsc(Integer productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select variant from ProductVariant variant where variant.id = :id")
    Optional<ProductVariant> findLockedById(@Param("id") Integer id);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndProductIdNot(String sku, Integer productId);

    void deleteByProductId(Integer productId);
}
