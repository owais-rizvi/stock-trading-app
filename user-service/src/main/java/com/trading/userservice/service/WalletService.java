package com.trading.userservice.service;

import com.trading.userservice.dto.UserResponse;
import com.trading.userservice.entity.User;
import com.trading.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class WalletService {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserResponse addFunds(String userId, BigDecimal amount) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds added: {} to user: {}", amount, userId);
        return userService.mapToResponse(savedUser);
    }

    public UserResponse deductFunds(String userId, BigDecimal amount) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        if (user.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance: " + user.getWalletBalance());
        }
        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds deducted: {} from user: {}", amount, userId);
        return userService.mapToResponse(savedUser);
    }

    public UserResponse creditFunds(String userId, BigDecimal amount) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds credited: {} to user: {}", amount, userId);
        return userService.mapToResponse(savedUser);
    }
}
