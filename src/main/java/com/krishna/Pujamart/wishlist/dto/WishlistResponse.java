package com.krishna.Pujamart.wishlist.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WishlistResponse {

    private UUID id;
    private UUID userId;
    private int totalItems;
    private List<WishlistItemResponse> items;
    private LocalDateTime updatedAt;
}
