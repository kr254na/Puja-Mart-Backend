package com.krishna.Pujamart.catalog.repository;

import com.krishna.Pujamart.catalog.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    boolean existsBySku(String sku);

    // Checks if the SKU exists on a variant belonging to another product
    boolean existsBySkuAndProductIdNot(String sku, UUID productId);

    // Checks if the SKU exists on any OTHER variant (excluding the current one being updated)
    boolean existsBySkuAndIdNot(String sku, UUID id);

    // Retrieve all variants associated with a specific product ID
    List<ProductVariant> findByProductId(UUID productId);
}
