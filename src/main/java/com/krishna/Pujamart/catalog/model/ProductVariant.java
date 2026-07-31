package com.krishna.Pujamart.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "product_variants",
        indexes = {
                @Index(name = "idx_variant_product", columnList = "product_id")
        },
        check = @CheckConstraint(
                name = "chk_variant_pricing_stock",
                constraint = "(base_price_override IS NULL OR base_price_override >= 0.01) AND " +
                        "(discount_price_override IS NULL OR (discount_price_override >= 0.00 AND (base_price_override IS NOT NULL AND discount_price_override <= base_price_override))) AND " +
                        "stock_quantity >= 0"
        )
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(unique = true, length = 100, nullable = false)
    private String sku; // Unique Stock Keeping Unit (e.g., PUJA-BRASS-IDOL-6IN)

    @Column(length = 50)
    private String size; // e.g., "Small", "Medium", "6 inches", "500g"
    @Column(length = 50)
    private String color;
    @Column(length = 100)
    private String material; // e.g., "Pure Brass", "Terracotta"

    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "base_price_override", precision = 10, scale = 2)
    private BigDecimal basePriceOverride;

    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "discount_price_override", precision = 10, scale = 2)
    private BigDecimal discountPriceOverride;

    @Min(0)
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    public String getName() {
        java.util.List<String> parts = new java.util.ArrayList<>();

        if (size != null && !size.isBlank()) {
            parts.add(size.trim());
        }
        if (color != null && !color.isBlank()) {
            parts.add(color.trim());
        }
        if (material != null && !material.isBlank()) {
            parts.add(material.trim());
        }

        return parts.isEmpty() ? "Standard" : String.join(", ", parts);
    }

}
