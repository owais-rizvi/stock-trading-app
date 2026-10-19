package com.trading.orderservice.repository;

import com.trading.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAt(String userId);
    Optional<Order> findByUserIdAndIdempotencyKey(String userId, String idempotencyKey);
}
