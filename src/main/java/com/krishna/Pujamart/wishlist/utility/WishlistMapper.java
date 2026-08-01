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

        String imageUrl = null;

        if (item.getKit() != null) {
            boolean inStock = item.getKit().getItems().stream()
                    .allMatch(kitItem -> {
                        int required = kitItem.getDefaultQuantity();
                        if (kitItem.getVariant() != null) {
                            return kitItem.getVariant().getStockQuantity() >= required;
                        }
                        return kitItem.getProduct().getStockQuantity() >= required;
                    });

            imageUrl = (item.getKit().getImageUrls() != null && !item.getKit().getImageUrls().isEmpty())
                    ? item.getKit().getImageUrls().get(0)
                    : null;
            builder.itemType("KIT")
                    .referenceId(item.getKit().getId())
                    .name(item.getKit().getName())
                    .sku(null)
                    .imageUrl(imageUrl)
                    .price(item.getKit().getActualPrice())
                    .inStock(inStock);
        } else if (item.getProduct() != null) {
            imageUrl = (item.getProduct().getImageUrls() != null && !item.getProduct().getImageUrls().isEmpty())
                    ? item.getProduct().getImageUrls().get(0)
                    : null;
            builder.itemType("PRODUCT")
                    .referenceId(item.getProduct().getId())
                    .name(item.getProduct().getName())
                    .imageUrl(imageUrl);

            if (item.getVariant() != null) {
                BigDecimal effectivePrice = null;

                if (item.getVariant().getDiscountPriceOverride() != null) {
                    effectivePrice = item.getVariant().getDiscountPriceOverride();
                } else if (item.getVariant().getBasePriceOverride() != null) {
                    effectivePrice = item.getVariant().getBasePriceOverride();
                } else if (item.getProduct() != null) {
                    effectivePrice = item.getProduct().getDiscountPrice() != null
                            ? item.getProduct().getDiscountPrice()
                            : item.getProduct().getPrice();
                }

                builder.sku(item.getVariant().getSku())
                        .price(effectivePrice)
                        .inStock(item.getVariant().getStockQuantity() > 0);
            }else {
                BigDecimal effectivePrice = item.getProduct().getDiscountPrice() != null
                        ? item.getProduct().getDiscountPrice()
                        : item.getProduct().getPrice();
                builder.sku(item.getProduct().getSku())
                        .price(effectivePrice)
                        .inStock(item.getProduct().getStockQuantity() > 0);
            }
        }

        return builder.build();
    }
}