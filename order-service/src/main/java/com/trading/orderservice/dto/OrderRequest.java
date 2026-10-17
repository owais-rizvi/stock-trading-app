package com.trading.orderservice.dto;

import com.trading.orderservice.entity.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String userId;

    @NotBlank(message = "Stock symbol is required")
    private String symbol;

    @NotNull(message = "Order type is required")
    private OrderType type;

    @Min(value = 1, message = "Quantity must be atleast 1")
    private int quantity;

}
