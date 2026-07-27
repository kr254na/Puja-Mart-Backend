package com.krishna.Pujamart.cart.utility;

import com.krishna.Pujamart.cart.dto.CartItemResponse;
import com.krishna.Pujamart.cart.dto.CartResponse;
import com.krishna.Pujamart.cart.model.Cart;
import com.krishna.Pujamart.cart.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "totalItems", source = "items", qualifiedByName = "calculateTotalItems")
    @Mapping(target = "totalAmount", source = "items", qualifiedByName = "calculateTotalAmount")
    CartResponse toCartResponse(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "variantName", source = "variant.name")
    @Mapping(target = "kitId", source = "kit.id")
    @Mapping(target = "kitName", source = "kit.name")
    @Mapping(target = "imageUrl", source = "item", qualifiedByName = "extractImageUrl")
    @Mapping(target = "unitPrice", expression = "java(item.getUnitPrice())")
    @Mapping(target = "totalPrice", expression = "java(item.getTotalPrice())")
    CartItemResponse toCartItemResponse(CartItem item);

    @Named("calculateTotalItems")
    default Integer calculateTotalItems(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Named("calculateTotalAmount")
    default BigDecimal calculateTotalAmount(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Named("extractImageUrl")
    default String extractImageUrl(CartItem item) {
        if (item == null) {
            return null;
        }

        if (item.getKit() != null && item.getKit().getImageUrls() != null && !item.getKit().getImageUrls().isEmpty()) {
            return item.getKit().getImageUrls().getFirst();
        }

        if(item.getProduct() != null && item.getProduct().getImageUrls() != null && !item.getProduct().getImageUrls().isEmpty()) {
            return item.getProduct().getImageUrls().getFirst();
        }
        return null;
    }
}
