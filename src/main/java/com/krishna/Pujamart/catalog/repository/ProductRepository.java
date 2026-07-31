package com.krishna.Pujamart.catalog.repository;

import com.krishna.Pujamart.catalog.model.Product;
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
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @EntityGraph(attributePaths = {"category", "deity","variants"})
    @Query(
            value = "SELECT p FROM Product p " +
                    "WHERE (:categoryId IS NULL OR p.category.id = :categoryId) " +
                    "AND (:deityId IS NULL OR p.deity.id = :deityId)",
            countQuery = "SELECT COUNT(p) FROM Product p " +
                    "WHERE (:categoryId IS NULL OR p.category.id = :categoryId) " +
                    "AND (:deityId IS NULL OR p.deity.id = :deityId)"
    )
    Page<Product> findFilteredCatalog(
            @Param("categoryId") UUID categoryId,
            @Param("deityId") UUID deityId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "deity", "variants"})
    Optional<Product> findProductWithDetailsById(UUID id);

    boolean existsByCategoryId(UUID id);
    boolean existsByDeityId(UUID id);
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, UUID  id);
}