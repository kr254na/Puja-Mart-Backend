package com.krishna.Pujamart.feedback.repository;

import com.krishna.Pujamart.feedback.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    boolean existsByUserIdAndKitId(UUID userId, UUID kitId);

    Page<Review> findByProductIdAndApprovedTrueOrderByCreatedAtDesc(UUID productId, Pageable pageable);

    Page<Review> findByKitIdAndApprovedTrueOrderByCreatedAtDesc(UUID kitId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.approved = true GROUP BY r.rating")
    List<Object[]> countReviewsByRatingForProduct(@Param("productId") UUID productId);

    // Check if user has a DELIVERED order containing this product
    @Query("""
        SELECT COUNT(i) > 0 FROM OrderItem i 
        JOIN i.order o 
        WHERE o.userId = :userId 
          AND o.orderStatus = com.krishna.Pujamart.order.enums.OrderStatus.DELIVERED 
          AND (i.product.id = :productId OR (i.variant IS NOT NULL AND i.variant.product.id = :productId))
    """)
    boolean hasDeliveredProductOrder(@Param("userId") UUID userId, @Param("productId") UUID productId);

    // Check if user has a DELIVERED order containing this kit
    @Query("""
        SELECT COUNT(i) > 0 FROM OrderItem i 
        JOIN i.order o 
        WHERE o.userId = :userId 
          AND o.orderStatus = com.krishna.Pujamart.order.enums.OrderStatus.DELIVERED 
          AND i.kit.id = :kitId
    """)
    boolean hasDeliveredKitOrder(@Param("userId") UUID userId, @Param("kitId") UUID kitId);
}
