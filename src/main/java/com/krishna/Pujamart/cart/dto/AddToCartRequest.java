package com.krishna.Pujamart.cart.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class AddToCartRequest {

    private UUID productId;
    private UUID variantId;
    private UUID kitId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;
}
