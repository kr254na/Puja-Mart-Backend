package com.krishna.Pujamart.kits.model;

import com.krishna.Pujamart.catalog.model.Deity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
        name = "puja_kits",
        indexes = {
                @Index(name = "idx_puja_kit_deity", columnList = "deity_id")
        },
        check = @CheckConstraint(
                name = "chk_puja_kit_pricing",
                constraint = "(base_price IS NULL OR base_price >= 0.01) AND " +
                        "(discount_price IS NULL OR (discount_price >= 0.00 AND (base_price IS NOT NULL AND discount_price <= base_price)))"
        )
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PujaKit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(nullable = false, length = 500, columnDefinition = "TEXT")
    private String description;

    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;

    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "is_customizable", nullable = false)
    @Builder.Default
    private Boolean isCustomizable = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ElementCollection
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "puja_kit_images", joinColumns = @JoinColumn(name = "kit_id"))
    @Column(name = "image_url", nullable = false)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deity_id")
    private Deity deity;

    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PujaKitItem> items = new ArrayList<>();

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

    public BigDecimal getActualPrice() {
        if (items == null || items.isEmpty()) {
            return discountPrice != null ? discountPrice : (basePrice != null ? basePrice : BigDecimal.ZERO);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (PujaKitItem item : items) {
            BigDecimal price = item.getEffectivePrice();
            if (price != null) {
                total = total.add(price.multiply(BigDecimal.valueOf(item.getDefaultQuantity())));
            }
        }
        return total;
    }

}