package com.krishna.Pujamart.kits.repository;

import com.krishna.Pujamart.kits.model.PujaKit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PujaKitRepository extends JpaRepository<PujaKit, UUID> {

    @EntityGraph(attributePaths = {"deity"})
    @Query(
            value = "SELECT k FROM PujaKit k " +
                    "WHERE (:deityId IS NULL OR k.deity.id = :deityId) " +
                    "AND k.active = true",
            countQuery = "SELECT COUNT(k) FROM PujaKit k " +
                    "WHERE (:deityId IS NULL OR k.deity.id = :deityId) " +
                    "AND k.active = true"
    )
    Page<PujaKit> findFilteredKits(@Param("deityId") UUID deityId, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @EntityGraph(attributePaths = {"deity", "items", "items.product", "items.variant"})
    @Query("SELECT k FROM PujaKit k WHERE k.id = :id AND k.active = true")
    Optional<PujaKit> findActiveByIdWithDetails(@Param("id") UUID id);
}
