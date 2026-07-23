package com.krishna.Pujamart.catalog.dto;

import com.krishna.Pujamart.catalog.enums.MeasurementUnit;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private String brand;
    private String sku;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private MeasurementUnit measurementUnit;
    private Boolean featured;
    private List<String> imageUrls;
    private UUID categoryId;
    private String categoryName;
    private UUID deityId;
    private String deityName;
    private List<ProductVariantResponse> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}