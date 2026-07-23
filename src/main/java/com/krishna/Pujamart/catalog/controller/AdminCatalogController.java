package com.krishna.Pujamart.catalog.controller;

import com.krishna.Pujamart.catalog.dto.*;
import com.krishna.Pujamart.catalog.service.CatalogService;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/catalog")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

    private final CatalogService catalogService;

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(catalogService.createProduct(request), HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(catalogService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(catalogService.deleteProduct(id));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        return new ResponseEntity<>(catalogService.createCategory(request), HttpStatus.CREATED);
    }

    @PostMapping("/deities")
    public ResponseEntity<ApiResponse<DeityResponse>> createDeity(@Valid @RequestBody DeityRequest request) {
        return new ResponseEntity<>(catalogService.createDeity(request), HttpStatus.CREATED);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable("id") UUID categoryId, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(catalogService.updateCategory(categoryId,request));
    }

    @PutMapping("/deities/{id}")
    public ResponseEntity<ApiResponse<DeityResponse>> updateDeity(@PathVariable("id") UUID deityId, @Valid @RequestBody DeityRequest request) {
        return ResponseEntity.ok(catalogService.updateDeity(deityId, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable("id") UUID categoryId) {
        return ResponseEntity.ok(catalogService.deleteCategory(categoryId));
    }

    @DeleteMapping("/deities/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDeity(@PathVariable("id") UUID detiyId) {
        return ResponseEntity.ok(catalogService.deleteDeity(detiyId));
    }
}
