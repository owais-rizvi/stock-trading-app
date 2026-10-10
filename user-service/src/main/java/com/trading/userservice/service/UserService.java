package com.trading.userservice.service;

import com.trading.userservice.dto.AuthResponse;
import com.trading.userservice.dto.RegisterRequest;
import com.trading.userservice.dto.UserResponse;
import com.trading.userservice.entity.User;
import com.trading.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final JwtService jwtService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String USER_REGISTERED_TOPIC = "user.registered";

    public AuthResponse register(RegisterRequest req) {
        log.info("Registering user: {}", req.getEmail());
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered " + req.getEmail());
        }
        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .walletBalance(req.getInitialDeposit() != null ? req.getInitialDeposit() : BigDecimal.valueOf(10000))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered: {}", savedUser.getId());

        Map<String, Object> userRegisteredEvent = new HashMap<>();
        userRegisteredEvent.put("userId", savedUser.getId());
        userRegisteredEvent.put("email", savedUser.getEmail());
        userRegisteredEvent.put("firstName", savedUser.getFirstName());
        userRegisteredEvent.put("lastName", savedUser.getLastName());
        userRegisteredEvent.put("walletBalance", savedUser.getWalletBalance());

        kafkaTemplate.send(USER_REGISTERED_TOPIC, savedUser.getId(), userRegisteredEvent).whenComplete((result, ex) -> {
            if (ex != null) log.error("Failed to publish user.registered for {}", savedUser.getId(), ex);
        });
        String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getEmail());
        String refreshToken = jwtService.generateRefreshToken(savedUser.getId());

        return authService.buildAuthResponse(savedUser, accessToken, refreshToken);
    }


    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return mapToResponse(user);
    }

    public UserResponse mapToResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setWalletBalance(user.getWalletBalance());
        userResponse.setStatus(user.getStatus());
        userResponse.setCreatedAt(user.getCreatedAt());

        return userResponse;
    }

}
