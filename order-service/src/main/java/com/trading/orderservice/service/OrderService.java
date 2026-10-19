package com.trading.orderservice.service;

import com.trading.orderservice.client.MarketDataClient;
import com.trading.orderservice.client.UserServiceClient;
import com.trading.orderservice.dto.OrderRequest;
import com.trading.orderservice.entity.Order;
import com.trading.orderservice.entity.OrderStatus;
import com.trading.orderservice.entity.OrderType;
import com.trading.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MarketDataClient marketDataClient;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate kafkaTemplate;

    private static final String ORDER_EXECUTED_TOPIC = "order.executed";
    private static final String ORDER_FAILED_TOPIC = "order.failed";

    public Order placeOrder(OrderRequest request, String idempotencyKey) {
        String userId = request.getUserId();

        Optional<Order> existing = orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate request, returning existing order {}", existing.get().getId());
            return existing.get();
        }

        Map<String, Object> priceData = marketDataClient.getStockPrice(request.getSymbol());
        BigDecimal currentPrice = new BigDecimal(priceData.get("price").toString());
        BigDecimal totalAmount = currentPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = new Order();
        order.setUserId(userId);
        order.setIdempotencyKey(idempotencyKey);
        order.setSymbol(request.getSymbol());
        order.setType(request.getType());
        order.setQuantity(request.getQuantity());
        order.setPrice(currentPrice);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);

        Order saved;
        try {
            saved = orderRepository.saveAndFlush(order);
        } catch (Exception e) {
            // a concurrent duplicate won the race
            return orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey).orElseThrow();
        }

        String reference = "ORDER:" + saved.getId();

        try {
            if (request.getType() == OrderType.BUY) {
                userServiceClient.deductFunds(userId, totalAmount, reference);
            } else {
                userServiceClient.creditFunds(userId, totalAmount, reference);
            }
        } catch (Exception e) {
            // timeout / 5xx: unknown whether money moved, so undo (safe: no-op if it didn't)
            return compensate(saved, reference, "Payment service error: " + e.getMessage());
        }

        try {
            saved.setStatus(OrderStatus.EXECUTED);
            saved.setExecutedAt(LocalDateTime.now());
            saved = orderRepository.save(saved);
        } catch (Exception e) {
            return compensate(saved, reference, "Could not finalize order: " + e.getMessage());
        }

        publishOrderEvent(ORDER_EXECUTED_TOPIC, saved, null);   // outside the try
        return saved;
    }

    private Order compensate(Order order, String reference, String reason) {
        try {
            userServiceClient.reverse(order.getUserId(), reference);
        } catch (Exception e) {
            log.error("REVERSAL FAILED for order {}, needs reconciliation", order.getId(), e);
            order.setStatus(OrderStatus.REVERSAL_PENDING);
            order.setFailureReason(reason);
            return orderRepository.save(order);
        }
        return markFailed(order, reason);
    }

    private Order markFailed(Order order, String reason) {
        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(reason);
        Order saved = orderRepository.save(order);
        publishOrderEvent(ORDER_FAILED_TOPIC, saved, reason);
        return saved;
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAt(userId);
    }

    private void publishOrderEvent(String topic,Order order, String reason) {

        Map<String, Object> event = new HashMap<>();
        event.put("orderId", order.getId());
        event.put("userId", order.getUserId());
        event.put("symbol", order.getSymbol());
        event.put("type", order.getType().name());
        event.put("quantity", order.getQuantity());
        event.put("price", order.getPrice());
        event.put("totalAmount", order.getTotalAmount());
        event.put("status", order.getStatus().name());

        if(reason != null) event.put("reason", reason);
        try {
            kafkaTemplate.send(topic, order.getId(), event);
            log.info("Event published {} for order: {}", topic, order.getId());
        } catch (Exception e) {
            log.error("Failed to publish {} for order {}", topic, order.getId(), e);
        }

    }
}
