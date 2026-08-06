package com.krishna.Pujamart.order.repository;

import com.krishna.Pujamart.order.enums.OrderStatus;
import com.krishna.Pujamart.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserIdOrderByPlacedAtDesc(UUID userId, Pageable pageable);

    Page<Order> findAllByOrderByPlacedAtDesc(Pageable pageable);

    Page<Order> findByOrderStatusOrderByPlacedAtDesc(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM Order o WHERE o.id IN :ids")
    List<Order> findAllWithItemsByIds(@Param("ids") List<UUID> ids);

    @EntityGraph(attributePaths = {
            "items",
            "items.product",
            "items.variant",
            "items.kit"
    })
    List<Order> findByOrderStatusAndPlacedAtBefore(OrderStatus orderStatus, LocalDateTime threshold);

    @EntityGraph(attributePaths = {
            "items",
            "items.product",
            "items.variant",
            "items.kit",
            "items.kit.items",
            "shipment"
    })
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findWithDetailsById(@Param("id") UUID id);
}
