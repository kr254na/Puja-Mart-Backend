package com.krishna.Pujamart.cart.repository;

import com.krishna.Pujamart.cart.model.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = {
            "items",
            "items.product",
            "items.variant",
            "items.kit",
            "items.kit.items",
            "items.kit.items.product",
            "items.kit.items.variant"
    })
    Optional<Cart> findByUserId(UUID userId);
}
