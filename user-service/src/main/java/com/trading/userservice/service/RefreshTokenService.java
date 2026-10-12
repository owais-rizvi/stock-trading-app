package com.trading.userservice.service;


import com.trading.userservice.entity.RefreshToken;
import com.trading.userservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken save(
            String userId,
            String jti,
            Instant expiresAt
    ) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .jti(jti)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
