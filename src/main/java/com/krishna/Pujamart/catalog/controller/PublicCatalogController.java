package com.krishna.Pujamart.catalog.controller;

import com.krishna.Pujamart.catalog.dto.CategoryResponse;
import com.krishna.Pujamart.catalog.dto.DeityResponse;
import com.krishna.Pujamart.catalog.dto.ProductResponse;
import com.krishna.Pujamart.catalog.model.*;
import com.krishna.Pujamart.catalog.service.CatalogService;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/catalog")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CatalogService catalogService;

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID deityId,
            Pageable pageable) {
        return ResponseEntity.ok(catalogService.getFilteredProducts(categoryId, deityId, pageable));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(catalogService.getProductById(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(catalogService.getAllCategories());
    }

    @GetMapping("/deities")
    public ResponseEntity<ApiResponse<List<DeityResponse>>> getDeities() {
        return ResponseEntity.ok(catalogService.getAllDeities());
    }
}