package com.krishna.Pujamart.catalog.model;

import com.krishna.Pujamart.catalog.enums.MeasurementUnit;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_deity", columnList = "deity_id")
        },
        check = @CheckConstraint(
            name = "chk_product_pricing_stock",
            constraint =
                "(price IS NULL OR price >= 0.01) AND " +
                        "(discount_price IS NULL OR " +
                        "(price IS NOT NULL " +
                        "AND discount_price >= 0.00 " +
                        "AND discount_price <= price)) AND " +
                        "stock_quantity >= 0"
            )
        )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String brand;

    @Column(unique = true, length = 100, nullable = false)
    private String sku; // Base SKU for products without variants

    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @DecimalMin("0.001")
    @Digits(integer = 8, fraction = 3)
    @Column(nullable = false, precision = 11, scale = 3)
    private BigDecimal weight;

    @Min(0)
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", length = 50)
    private MeasurementUnit measurementUnit;

    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @ElementCollection
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", nullable = false)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "deity_id", nullable = false)
    private Deity deity;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}