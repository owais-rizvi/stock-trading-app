package com.trading.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String userId;

    private String email;

    private String firstName;

    private String lastName;

    private BigDecimal walletBalance;

    private String accessToken;

    @JsonIgnore
    private String refreshToken;

}
