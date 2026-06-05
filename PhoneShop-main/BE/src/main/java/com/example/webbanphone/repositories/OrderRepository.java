package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserIdOrderByIdDesc(Integer userId);

    List<Order> findAllByOrderByIdDesc();

    List<Order> findByStatusOrderByIdDesc(String status);

    long countByUserId(Integer userId);
}
