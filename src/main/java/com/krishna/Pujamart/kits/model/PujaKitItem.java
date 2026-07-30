package com.krishna.Pujamart.kits.model;

import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "puja_kit_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PujaKitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kit_id", nullable = false)
    private PujaKit kit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Min(1)
    @Column(name = "default_quantity", nullable = false)
    @Builder.Default
    private Integer defaultQuantity = 1;

    @Min(1)
    @Column(name = "min_quantity", nullable = false)
    @Builder.Default
    private Integer minQuantity = 1;

    @Min(1)
    @Column(name = "max_quantity")
    private Integer maxQuantity;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = true;

    public BigDecimal getEffectivePrice() {
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
        return null;
    }
}