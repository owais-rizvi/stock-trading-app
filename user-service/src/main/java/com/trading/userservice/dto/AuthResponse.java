package com.trading.userservice.dto;

import com.trading.userservice.entity.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String userId;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private BigDecimal walletBalance;

    private String accessToken;

    private String refreshToken;

    private String tokenType = "Bearer";

}
