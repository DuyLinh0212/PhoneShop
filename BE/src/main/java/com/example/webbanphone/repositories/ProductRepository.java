package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
