package com.krishna.Pujamart.kits.dto;

import com.krishna.Pujamart.catalog.dto.ProductResponse;
import com.krishna.Pujamart.catalog.dto.ProductVariantResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PujaKitItemResponse {

    private UUID id;
    private ProductResponse productResponse;
    private ProductVariantResponse productVariantResponse;
    private Integer defaultQuantity;
    private Integer minQuantity;
    private Integer maxQuantity;
    private Boolean isMandatory;
}
