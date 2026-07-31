package com.krishna.Pujamart.wishlist.repository;

import com.krishna.Pujamart.wishlist.model.Wishlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    @EntityGraph(attributePaths = {"items", "items.product", "items.variant", "items.kit"})
    Optional<Wishlist> findByUserId(UUID userId);
}
