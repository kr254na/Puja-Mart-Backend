package com.krishna.Pujamart.catalog.service;

import com.krishna.Pujamart.catalog.dto.*;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

}
