package com.krishna.Pujamart.wishlist.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.UUID;

@Data
public class AddToWishlistRequest {

    private UUID productId;
    private UUID variantId;
    private UUID kitId;

    @AssertTrue(message = "Item must be either a Puja Kit OR a Product (with optional variant)")
    public boolean isValidItemType() {
        boolean isKit = kitId != null && productId == null && variantId == null;
        boolean isProduct = kitId == null && productId != null;
        return isKit || isProduct;
    }
}