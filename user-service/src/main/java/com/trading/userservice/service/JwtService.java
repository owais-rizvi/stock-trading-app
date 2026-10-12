package com.trading.userservice.service;

import com.trading.userservice.dto.GeneratedRefreshToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
@Slf4j

public class JwtService {

    private final long jwtRefreshExpiration;

    private final long jwtExpiration;

    private final String secret;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.accessExp}") String jwtExpiration,
            @Value("${jwt.refreshExp}") String jwtRefreshExpiration) {
        this.secret = secret;
        this.jwtExpiration = Long.parseLong(jwtExpiration);
        this.jwtRefreshExpiration = Long.parseLong(jwtRefreshExpiration);
    }

    public String generateAccessToken(String userId, String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("type", "access")
                .subject(userId)
                .claim("email", email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }
    public GeneratedRefreshToken generateRefreshToken(String userId) {
        String jti = java.util.UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .id(jti)
                .claim("type", "refresh")
                .subject(userId)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtRefreshExpiration))
                .signWith(getSignInKey())
                .compact();

        return new GeneratedRefreshToken(token, jti);

    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Instant getRefreshTokenExpiry() {
        return Instant.now().plusMillis(jwtRefreshExpiration);
    }

}


