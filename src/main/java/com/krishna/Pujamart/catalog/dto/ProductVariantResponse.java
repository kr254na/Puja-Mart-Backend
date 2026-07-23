package com.krishna.Pujamart.catalog.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductVariantResponse {
    private UUID id;
    private String sku;
    private String size;
    private String color;
    private String material;
    private BigDecimal priceOverride;
    private Integer stockQuantity;
}
