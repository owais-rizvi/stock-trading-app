package com.trading.userservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class JwtService {

    public String generateAccessToken(String userId, String email) {
        return "";
    }
    public String generateRefreshToken(String userId) {
        return "";
    }
}
