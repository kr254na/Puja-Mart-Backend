package com.krishna.Pujamart.kits.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class PujaKitItemRequest {

    private UUID productId;
    private UUID variantId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer defaultQuantity = 1;

    @Min(value = 1, message = "Minimum Quantity cannot be less than 1")
    private Integer minQuantity=1;
    @Min(value = 1, message = "Maximum Quantity must be at least 1")
    private Integer maxQuantity;

    private Boolean isMandatory = true;
}
