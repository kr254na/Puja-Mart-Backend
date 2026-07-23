package com.krishna.Pujamart.catalog.controller;

import com.krishna.Pujamart.catalog.dto.ProductVariantResponse;
import com.krishna.Pujamart.catalog.service.ProductVariantService;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/catalog")
@RequiredArgsConstructor
public class PublicProductVariantController {

    private final ProductVariantService productVariantService;

    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariantsByProductId(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(productVariantService.getVariantsByProductId(productId));
    }

    @GetMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getVariantById(
            @PathVariable UUID variantId) {
        return ResponseEntity.ok(productVariantService.getVariantById(variantId));
    }
}

