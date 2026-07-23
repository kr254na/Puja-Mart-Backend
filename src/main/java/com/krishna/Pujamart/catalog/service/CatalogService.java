package com.krishna.Pujamart.catalog.service;

import com.krishna.Pujamart.catalog.dto.*;
import com.krishna.Pujamart.catalog.model.*;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface CatalogService {

    ApiResponse<Page<ProductResponse>> getFilteredProducts(UUID categoryId, UUID deityId, Pageable pageable);
    ApiResponse<ProductResponse> getProductById(UUID id);
    ApiResponse<List<CategoryResponse>> getAllCategories();
    ApiResponse<List<DeityResponse>> getAllDeities();

    ApiResponse<ProductResponse> createProduct(ProductRequest request);
    ApiResponse<ProductResponse> updateProduct(UUID id, ProductRequest request);
    ApiResponse<Void> deleteProduct(UUID id);

    ApiResponse<CategoryResponse> createCategory(CategoryRequest request);
    ApiResponse<DeityResponse> createDeity(DeityRequest request);
    ApiResponse<CategoryResponse> updateCategory(UUID id, CategoryRequest request);
    ApiResponse<DeityResponse> updateDeity(UUID id, DeityRequest request);
    ApiResponse<Void> deleteCategory(UUID id);
    ApiResponse<Void>  deleteDeity(UUID id);

    default ProductResponse mapProductToProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        List<ProductVariantResponse> variantResponses = new ArrayList<>();
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            variantResponses = product.getVariants().stream()
                    .map(this::mapVariantToVariantResponse)
                    .toList();
        }

        UUID categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;

        UUID deityId = product.getDeity() != null ? product.getDeity().getId() : null;
        String deityName = product.getDeity() != null ? product.getDeity().getName() : null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .sku(product.getSku())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .stockQuantity(product.getStockQuantity())
                .measurementUnit(product.getMeasurementUnit())
                .featured(product.getFeatured() != null ? product.getFeatured() : false)
                .imageUrls(product.getImageUrls() != null ? new ArrayList<>(product.getImageUrls()) : new ArrayList<>())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .deityId(deityId)
                .deityName(deityName)
                .variants(variantResponses)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    default ProductVariantResponse mapVariantToVariantResponse(ProductVariant variant) {
        if (variant == null) {
            return null;
        }

        return ProductVariantResponse.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .size(variant.getSize())
                .color(variant.getColor())
                .material(variant.getMaterial())
                .priceOverride(variant.getPriceOverride())
                .stockQuantity(variant.getStockQuantity())
                .build();
    }

    default CategoryResponse mapCategoryToCategoryResponse(Category category) {
        if(category == null) {
            return null;
        }
        return CategoryResponse
                .builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    default DeityResponse mapDeityToDeityResponse(Deity deity) {
        if(deity == null) {
            return null;
        }
        return DeityResponse
                .builder()
                .id(deity.getId())
                .name(deity.getName())
                .description(deity.getDescription())
                .build();
    }

    default <T> ApiResponse<T> mapToApiResponse(
            boolean success,
            String message,
            T data
    ) {
        return ApiResponse.<T>builder()
                .success(success)
                .message(message)
                .data(data)
                .build();
    }

}
