package com.krishna.Pujamart.cart.repository;

import com.krishna.Pujamart.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    // Check if the product itself is in any cart
    boolean existsByProductId(UUID productId);

    // Check if the kit is in any cart
    boolean existsByKitId(UUID kitId);

    // Check if the variant is in any cart
    boolean existsByVariantId(UUID variantId);

    // Check if any variant in a cart relies on this product's base price (has no overrides)
    @Query("SELECT COUNT(ci) > 0 FROM CartItem ci " +
            "WHERE ci.variant.product.id = :productId " +
            "AND ci.variant.basePriceOverride IS NULL " +
            "AND ci.variant.discountPriceOverride IS NULL")
    boolean existsPricelessVariantInCart(@Param("productId") UUID productId);
}

