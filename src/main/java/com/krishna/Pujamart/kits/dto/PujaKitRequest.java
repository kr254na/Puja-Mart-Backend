package com.krishna.Pujamart.kits.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class PujaKitRequest {

    @NotBlank(message = "Kit name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Kit description is required")
    private String description;

    @DecimalMin("0.01")
    private BigDecimal basePrice;

    @DecimalMin("0.00")
    private BigDecimal discountPrice;

    private Boolean isCustomizable = true;

    private Boolean active = true;

    private UUID deityId;

    private List<String> imageUrls;

    @NotEmpty(message = "A Puja Kit must contain at least one item")
    @Valid
    private List<PujaKitItemRequest> items;
}
