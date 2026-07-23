package com.krishna.Pujamart.catalog.service;

import com.krishna.Pujamart.catalog.dto.ProductVariantRequest;
import com.krishna.Pujamart.catalog.dto.ProductVariantResponse;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import com.krishna.Pujamart.identity.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface ProductVariantService {
    ApiResponse<ProductVariantResponse> addVariant(UUID productId, ProductVariantRequest request);
    ApiResponse<ProductVariantResponse> updateVariant(UUID variantId, ProductVariantRequest request);
    ApiResponse<Void> deleteVariant(UUID variantId);
    ApiResponse<List<ProductVariantResponse>> getVariantsByProductId(UUID productId);
    ApiResponse<ProductVariantResponse> getVariantById(UUID variantId);
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

    default  <T> ApiResponse<T> mapToApiResponse(boolean success, String message, T data) {
        return ApiResponse.<T>builder()
                .success(success)
                .message(message)
                .data(data)
                .build();
    }
}

