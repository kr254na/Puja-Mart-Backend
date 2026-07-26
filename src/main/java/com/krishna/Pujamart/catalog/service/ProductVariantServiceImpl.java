package com.krishna.Pujamart.catalog.service;

import com.krishna.Pujamart.catalog.dto.ProductVariantRequest;
import com.krishna.Pujamart.catalog.dto.ProductVariantResponse;
import com.krishna.Pujamart.catalog.exception.DuplicateSkuException;
import com.krishna.Pujamart.catalog.exception.ProductNotFoundException;
import com.krishna.Pujamart.catalog.exception.ProductVariantNotFoundException;
import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import com.krishna.Pujamart.catalog.repository.ProductRepository;
import com.krishna.Pujamart.catalog.repository.ProductVariantRepository;
import com.krishna.Pujamart.catalog.utility.ProductMapper;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ApiResponse<ProductVariantResponse> addVariant(UUID productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        String variantSku = resolveOrGenerateVariantSku(
                request.getSku(),
                product.getName(),
                null
        );
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(variantSku)
                .size(request.getSize())
                .color(request.getColor())
                .material(request.getMaterial())
                .priceOverride(request.getPriceOverride())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .build();

        ProductVariant savedVariant = productVariantRepository.save(variant);

        return ApiResponse.success(
                "Product variant added successfully",
                productMapper.toVariantResponse(savedVariant)
        );
    }

    @Override
    @Transactional
    public ApiResponse<ProductVariantResponse> updateVariant(UUID variantId, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product variant not found with ID: " + variantId));

        Product product = variant.getProduct();

        String variantSku = resolveOrGenerateVariantSku(
                request.getSku(),
                product.getName(),
                variant.getId()
        );

        variant.setSku(variantSku);
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setMaterial(request.getMaterial());
        variant.setPriceOverride(request.getPriceOverride());
        variant.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);

        ProductVariant updatedVariant = productVariantRepository.save(variant);

        return ApiResponse.success(
                "Product variant updated successfully",
                productMapper.toVariantResponse(updatedVariant)
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteVariant(UUID variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new ProductVariantNotFoundException("Product variant not found with ID: " + variantId);
        }
        productVariantRepository.deleteById(variantId);

        return ApiResponse.success("Product variant deleted successfully");
    }

    @Override
    public ApiResponse<List<ProductVariantResponse>> getVariantsByProductId(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }

        List<ProductVariantResponse> responses = productVariantRepository.findByProductId(productId)
                .stream()
                .map(productMapper::toVariantResponse)
                .toList();

        return ApiResponse.success(
                "Product variants fetched successfully",
                responses
        );
    }

    @Override
    public ApiResponse<ProductVariantResponse> getVariantById(UUID variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product variant not found with ID: " + variantId));

        return ApiResponse.success(
                "Product variant fetched successfully",
                productMapper.toVariantResponse(variant)
        );
    }

    private String resolveOrGenerateVariantSku(String requestedSku, String contextTitle, UUID currentVariantId) {
        // 1. Validate manual SKU if provided
        if (requestedSku != null && !requestedSku.isBlank()) {
            String cleanSku = requestedSku.trim().toUpperCase();

            boolean existsInProducts = productRepository.existsBySku(cleanSku);

            boolean existsInVariants = (currentVariantId == null)
                    ? productVariantRepository.existsBySku(cleanSku)
                    : productVariantRepository.existsBySkuAndIdNot(cleanSku, currentVariantId);

            if (existsInProducts || existsInVariants) {
                throw new DuplicateSkuException("SKU already exists: " + cleanSku);
            }
            return cleanSku;
        }

        // 2. Auto-generate fallback SKU (e.g., PJM-VAR-8F2A)
        String prefix = "PJM";
        String titleSlug = contextTitle.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        String titleCode = (titleSlug.length() >= 3) ? titleSlug.substring(0, 3) : "VAR";

        String generatedSku;
        boolean isTaken;

        do {
            String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            generatedSku = String.format("%s-%s-%s", prefix, titleCode, uniqueSuffix);

            boolean takenInProducts = productRepository.existsBySku(generatedSku);

            boolean takenInVariants = (currentVariantId == null)
                    ? productVariantRepository.existsBySku(generatedSku)
                    : productVariantRepository.existsBySkuAndIdNot(generatedSku, currentVariantId);

            isTaken = takenInProducts || takenInVariants;

        } while (isTaken);

        return generatedSku;
    }
}

