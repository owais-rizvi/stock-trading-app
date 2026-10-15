package com.trading.marketservice.controller;

import com.trading.marketservice.dto.StockPrice;
import com.trading.marketservice.entity.Stock;
import com.trading.marketservice.service.MarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
@Slf4j
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;

    @GetMapping("/stocks")
    public ResponseEntity<List<StockPrice>> getAllStocks() {
        return ResponseEntity.ok(
                marketService.getAllPrices()
        );
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<StockPrice> getStockPrice(@PathVariable String symbol) {
        return ResponseEntity.ok(
                marketService.getPrice(symbol.toUpperCase())
        );
    }

    @GetMapping("/stocks/list")
    public ResponseEntity<List<Stock>> getStockList() {
        return ResponseEntity.ok(marketService.getAllStocks());
    }

}
