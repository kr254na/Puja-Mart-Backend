package com.krishna.Pujamart.catalog.controller;

import com.krishna.Pujamart.catalog.dto.ProductVariantRequest;
import com.krishna.Pujamart.catalog.dto.ProductVariantResponse;
import com.krishna.Pujamart.catalog.service.ProductVariantService;
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
public class AdminProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> addVariant(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductVariantRequest request) {
        return new ResponseEntity<>(
                productVariantService.addVariant(productId, request),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable UUID variantId,
            @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(productVariantService.updateVariant(variantId, request));
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable UUID variantId) {
        return ResponseEntity.ok(productVariantService.deleteVariant(variantId));
    }
}
