package com.krishna.Pujamart.catalog.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductVariantRequest {

    @Pattern(regexp = "^[A-Za-z0-9-_]+$", message = "SKU must contain only alphanumeric characters, dashes, or underscores")
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;
    @Size(max = 50, message = "Size cannot exceed 50 characters")
    private String size;
    @Size(max = 50, message = "Color cannot exceed 50 characters")
    private String color;
    @Size(max = 100, message = "Material cannot exceed 100 characters")
    private String material;

    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2,
            message = "Base Price can have up to 8 digits and 2 decimal places")
    private BigDecimal basePriceOverride;

    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2,
            message = "Discount Price can have up to 8 digits and 2 decimal places")
    private BigDecimal discountPriceOverride;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @NotNull
    private Integer stockQuantity = 0;
}
