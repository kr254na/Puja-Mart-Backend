package com.krishna.Pujamart.cart.model;

import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import com.krishna.Pujamart.kits.model.PujaKit;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items",
                indexes = {
                        @Index(name = "idx_cart_item_product", columnList = "product_id"),
                        @Index(name = "idx_cart_item_variant", columnList = "variant_id"),
                        @Index(name = "idx_cart_item_kit", columnList = "kit_id")
        },
                check = @CheckConstraint(
                        name = "chk_cart_item_type",
                        constraint = "(kit_id IS NOT NULL AND product_id IS NULL AND variant_id IS NULL) OR " +
                                "(kit_id IS NULL AND product_id IS NOT NULL)"
                )
        )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kit_id")
    private PujaKit kit;

    @Min(1)
    @Builder.Default
    @Column(
            nullable = false,
            check = @CheckConstraint(name = "chk_cart_item_qty", constraint = "quantity >= 1")
    )
    private Integer quantity = 1;

    public BigDecimal getUnitPrice() {
        if (kit != null) {
            return kit.getActualPrice();
        }

        if (variant != null) {
            if (variant.getDiscountPriceOverride() != null) {
                return variant.getDiscountPriceOverride();
            }
            if (variant.getBasePriceOverride() != null) {
                return variant.getBasePriceOverride();
            }
        }

        if (product != null) {
            return product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
        }

        return BigDecimal.ZERO;
    }


    public BigDecimal getTotalPrice() {
        return getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
