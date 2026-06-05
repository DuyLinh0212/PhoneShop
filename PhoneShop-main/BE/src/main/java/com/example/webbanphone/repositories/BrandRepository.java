package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findByNameIgnoreCase(String name);
}
