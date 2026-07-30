package com.krishna.Pujamart.kits.dto;

import com.krishna.Pujamart.catalog.dto.DeityResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PujaKitResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private Boolean active;
    @Builder.Default
    private Boolean isCustomizable=true;
    private List<String> imageUrls;

    private DeityResponse deity;

    private List<PujaKitItemResponse> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal originalPrice;
    private Boolean hasMissingPrices;

}
