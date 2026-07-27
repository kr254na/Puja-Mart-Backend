package com.krishna.Pujamart.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID id;
    private UUID userId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal totalAmount;
}