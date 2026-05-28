package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Integer> {
}
