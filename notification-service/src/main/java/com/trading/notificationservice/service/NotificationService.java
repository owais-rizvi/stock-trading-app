package com.trading.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class NotificationService {
    @KafkaListener(topics = "order.executed")
    public void consumeOrderExecuted(@Payload Map<String, Object> payload) {
        try {
            String userId = payload.get("userId").toString();
            String type = payload.get("type").toString();
            String symbol = payload.get("symbol").toString();
            Object quantity = payload.get("quantity");
            Object price = payload.get("price");
            Object totalAmount = payload.get("totalAmount");

            sendNotification(userId, type.equals("BUY") ? "ORDER EXECUTED - BUY" : "ORDER BY - SELL", String.format("%s %s shared of %s at %s. Total: %s", type, quantity, symbol, price, totalAmount));
        }
        catch(Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    public void consumeOrderFailed(@Payload Map<String, Object> payload) {
        try {
            String userId = payload.get("userId").toString();
            String symbol = payload.get("symbol").toString();
            String reason = payload.get("reason").toString();

            sendNotification(userId, "Order failed", String.format("Your order for %s failed. Reason: %s", symbol, reason));
        }
        catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    public void consumeUserRegistered(@Payload Map<String, Object> payload) {
        try {
            String userId = payload.get("userId").toString();
            String firstName = payload.get("firstName").toString();
            Object walletBalance = payload.get("walletBalance").toString();

            sendNotification(userId, "Welcome to the Stock Trading Platform", String.format("Welcome %s, %s added to your wallet", firstName, walletBalance));
        }
        catch(Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    private void sendNotification(String userId, String title, String message) {
        log.info("------------------------------------------");
        log.info("NOTIFICATION SENT");
        log.info("TO USER: {}", userId);
        log.info("TITLE: {}", title);
        log.info("MESSAGE: {}", message);
        log.info("------------------------------------------");
    }
}
