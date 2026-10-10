package com.trading.userservice.service;

import com.trading.userservice.dto.AuthResponse;
import com.trading.userservice.dto.LoginRequest;
import com.trading.userservice.entity.User;
import com.trading.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .walletBalance(user.getWalletBalance())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        log.info("Logging in user: {}", req.getEmail());
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow(() -> new RuntimeException("User not found" + req.getEmail()));

        if(!passwordEncoder.matches(
                req.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid credentials");
        }
        log.info("Login successful: {}", user.getId());
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

}
