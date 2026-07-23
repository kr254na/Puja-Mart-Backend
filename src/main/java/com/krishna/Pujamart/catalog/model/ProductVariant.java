package com.krishna.Pujamart.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
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
    @Column(precision = 10, scale = 2)
    private BigDecimal priceOverride; // Optional: Override base product price for larger variants

    @Min(0)
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;
}
