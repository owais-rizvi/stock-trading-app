package com.trading.marketservice.repository;

import com.trading.marketservice.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, String> {
    List<Stock> findByActiveTrue();
}
