package com.krishna.Pujamart.wishlist.utility;

import com.krishna.Pujamart.wishlist.dto.WishlistItemResponse;
import com.krishna.Pujamart.wishlist.dto.WishlistResponse;
import com.krishna.Pujamart.wishlist.model.Wishlist;
import com.krishna.Pujamart.wishlist.model.WishlistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(target = "totalItems", expression = "java(wishlist.getItems() != null ? wishlist.getItems().size() : 0)")
    WishlistResponse toWishlistResponse(Wishlist wishlist);

    List<WishlistItemResponse> toWishlistItemResponseList(List<WishlistItem> items);

    default WishlistItemResponse toWishlistItemResponse(WishlistItem item) {
        if (item == null) return null;

        WishlistItemResponse.WishlistItemResponseBuilder builder = WishlistItemResponse.builder()
                .id(item.getId())
                .addedAt(item.getAddedAt());

        if (item.getKit() != null) {

            builder.itemType("KIT")
                    .referenceId(item.getKit().getId())
                    .name(item.getKit().getName())
                    .sku(null)
                    .price(item.getKit().getActualPrice())
                    .inStock(true);
        } else if (item.getProduct() != null) {
            builder.itemType("PRODUCT")
                    .referenceId(item.getProduct().getId())
                    .name(item.getProduct().getName())
                    .imageUrl(item.getProduct().getImageUrls().getFirst());

            if (item.getVariant() != null) {
                builder.sku(item.getVariant().getSku())
                        .price(item.getVariant().getDiscountPriceOverride())
                        .inStock(item.getVariant().getStockQuantity() > 0);
            } else {
                builder.sku(item.getProduct().getSku())
                        .price(item.getProduct().getDiscountPrice())
                        .inStock(item.getProduct().getStockQuantity() > 0);
            }
        }

        return builder.build();
    }
}