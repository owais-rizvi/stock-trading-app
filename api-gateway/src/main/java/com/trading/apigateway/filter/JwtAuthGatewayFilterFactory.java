package com.trading.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
@Slf4j
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public JwtAuthGatewayFilterFactory() {
        super(Config.class);
    }
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

//            PUBLIC ENDPOINTS
            if(isPublicEndpoint(path)) {
                    log.debug("Public endpoint - skip JWT");
            }
            if (!request.getHeaders().containsHeader("Authorization")) {
                log.warn("Missing authorization header: {}", path);
                return unauthorizedResponse(exchange, "Authorization header missing");
            }
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorizedResponse(exchange, "Invalid authorization format");
            }
            String token = authHeader.substring(7);
            try {
                Claims claims = extractClaims(token);
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                log.debug("User id: {}, email: {}", userId, email);

                ServerHttpRequest modifiedRequest = request.mutate().header("X-User-Id", userId).build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
            catch(Exception e) {
                log.warn("Invalid JWT Token: {}", e.getMessage());
                return unauthorizedResponse(exchange, "Invalid or expired token");
            }
        });
    }

    private Claims extractClaims(String token) {
        return Jwts.parser().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private boolean isPublicEndpoint(String path) {
        return path.contains("/register") || path.contains("/login") || path.contains("/health") || path.contains("/ws");
    }

    public static class Config{

    }
}
