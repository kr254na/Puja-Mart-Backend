package com.krishna.Pujamart.catalog.dto;

import com.krishna.Pujamart.catalog.enums.MeasurementUnit;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    private String name;

    @NotBlank(message = "Product description cannot be blank")
    private String description;

    @Size(max = 100, message = "Brand name cannot exceed 100 characters")
    private String brand;

    @Pattern(regexp = "^[A-Za-z0-9-_]+$", message = "SKU must contain only alphanumeric characters, dashes, or underscores")
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;

    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2,
            message = "Price can have up to 8 digits and 2 decimal places")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Discount price cannot be negative")
    @Digits(integer = 8, fraction = 2,
            message = "Discount Price can have up to 8 digits and 2 decimal places")
    private BigDecimal discountPrice;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    private MeasurementUnit measurementUnit;

    private Boolean featured = false;

    @Size(max = 5, message = "You can attach up to 5 image URLs per product")
    private List<@NotBlank(message = "Image URL cannot be blank") String> imageUrls;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Deity ID is required")
    private UUID deityId;
}