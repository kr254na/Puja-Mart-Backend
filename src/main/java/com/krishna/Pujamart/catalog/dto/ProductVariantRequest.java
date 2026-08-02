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

    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2,
            message = "Base Price can have up to 8 digits and 2 decimal places")
    private BigDecimal basePriceOverride;

    @DecimalMin(value = "0.00", message = "Discount price cannot be negative")
    @Digits(integer = 8, fraction = 2,
            message = "Discount Price can have up to 8 digits and 2 decimal places")
    private BigDecimal discountPriceOverride;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.001", message = "Weight must be at least 0.001 kg")
    @Digits(integer = 8, fraction = 3,
            message = "Weight can have up to 8 digits and 3 decimal places")
    private BigDecimal weight;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @NotNull
    private Integer stockQuantity = 0;
}
