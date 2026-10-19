package com.trading.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {

    @PostMapping("/api/v1/users/funds/add")
    Map<String, Object> addFunds(@RequestHeader("X-User-Id") String userId, @RequestParam BigDecimal amount, @RequestParam String reference);

    @PostMapping("/api/v1/users/funds/deduct")
    Map<String, Object> deductFunds(@RequestHeader("X-User-Id") String userId, @RequestParam BigDecimal amount, @RequestParam String reference);

    @PostMapping("/api/v1/users/funds/credit")
    Map<String, Object> creditFunds(@RequestHeader("X-User-Id") String userId, @RequestParam BigDecimal amount, @RequestParam String reference);

    @PostMapping("/api/v1/users/funds/reverse")
    Map<String, Object> reverse(@RequestHeader("X-User-Id") String userId, @RequestParam String reference);
}
