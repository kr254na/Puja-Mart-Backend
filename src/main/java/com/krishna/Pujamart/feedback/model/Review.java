package com.krishna.Pujamart.feedback.model;

import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.kits.model.PujaKit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_reviews",
        indexes = {
                @Index(name = "idx_review_product", columnList = "product_id"),
                @Index(name = "idx_review_kit", columnList = "kit_id"),
                @Index(name = "idx_review_user", columnList = "user_id")
        },
        uniqueConstraints = {
                // Ensures a user can only review a product/kit once
                @UniqueConstraint(name = "uk_user_product_review", columnNames = {"user_id", "product_id"}),
                @UniqueConstraint(name = "uk_user_kit_review", columnNames = {"user_id", "kit_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kit_id")
    private PujaKit kit;

    @Column(nullable = false)
    private Integer rating; // 1 to 5 stars

    @Column(length = 100)
    private String title;

    @Column(length = 1000)
    private String comment;

    @Column(name = "is_verified_purchase", nullable = false)
    private boolean verifiedPurchase;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean approved = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
