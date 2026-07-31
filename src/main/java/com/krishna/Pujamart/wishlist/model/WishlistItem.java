package com.krishna.Pujamart.wishlist.model;

import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import com.krishna.Pujamart.kits.model.PujaKit;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wishlist_items",
        indexes = {
                @Index(name = "idx_wishlist_product", columnList = "wishlist_id, product_id"),
                @Index(name = "idx_wishlist_variant", columnList = "wishlist_id, variant_id"),
                @Index(name = "idx_wishlist_kit", columnList = "wishlist_id, kit_id")
        },
        check = @CheckConstraint(
                name = "chk_product_pricing_stock",
                constraint = "price >= 0.01 AND " +
                        "(discount_price IS NULL OR (discount_price >= 0.00 AND discount_price <= price)) AND " +
                        "stock_quantity >= 0"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kit_id")
    private PujaKit kit;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
    }
}
