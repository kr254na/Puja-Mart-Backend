package com.krishna.Pujamart.catalog.service;

import com.krishna.Pujamart.catalog.dto.ProductVariantRequest;
import com.krishna.Pujamart.catalog.dto.ProductVariantResponse;
import com.krishna.Pujamart.identity.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface ProductVariantService {
    ApiResponse<ProductVariantResponse> addVariant(UUID productId, ProductVariantRequest request);
    ApiResponse<ProductVariantResponse> updateVariant(UUID variantId, ProductVariantRequest request);
    ApiResponse<Void> deleteVariant(UUID variantId);
    ApiResponse<List<ProductVariantResponse>> getVariantsByProductId(UUID productId);
    ApiResponse<ProductVariantResponse> getVariantById(UUID variantId);
}

