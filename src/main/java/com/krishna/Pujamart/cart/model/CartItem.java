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
@Table(name = "cart_items")
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
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    public BigDecimal getUnitPrice() {
        if (kit != null) {
            return kit.getDiscountPrice() != null ? kit.getDiscountPrice() : kit.getBasePrice();
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
