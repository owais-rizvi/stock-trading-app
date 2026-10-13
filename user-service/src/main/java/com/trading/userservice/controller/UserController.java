package com.trading.userservice.controller;

import com.trading.userservice.dto.AuthResponse;
import com.trading.userservice.dto.LoginRequest;
import com.trading.userservice.dto.RegisterRequest;
import com.trading.userservice.dto.UserResponse;
import com.trading.userservice.service.AuthService;
import com.trading.userservice.service.UserService;
import com.trading.userservice.service.WalletService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final WalletService walletService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(req));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletResponse res) {
        AuthResponse authResponse = authService.login(req);

        ResponseCookie cookie = ResponseCookie.from(
                        "refreshToken",
                        authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false) // true in production with HTTPS
                .sameSite("Lax")
                .path("/api/auth/")
                .maxAge(Duration.ofDays(7))
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping("/{userId}/funds/add")
    public ResponseEntity<UserResponse> addFunds(@PathVariable String userId, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.addFunds(userId, amount));
    }

    @PostMapping("/{userId}/funds/deduct")
    public ResponseEntity<UserResponse> deductFunds(@PathVariable String userId, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.deductFunds(userId, amount));
    }

    @PostMapping("/{userId}/funds/credit")
    public ResponseEntity<UserResponse> creditFunds(@PathVariable String userId, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.creditFunds(userId, amount));
    }
}

