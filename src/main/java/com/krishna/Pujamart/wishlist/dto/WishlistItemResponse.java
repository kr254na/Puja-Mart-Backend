package com.krishna.Pujamart.wishlist.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WishlistItemResponse {

    private UUID id;
    private String itemType; // "PRODUCT" or "KIT"
    private UUID referenceId;
    private String name;
    private String sku;
    private BigDecimal price;
    private String imageUrl;
    private boolean inStock;
    private LocalDateTime addedAt;
}