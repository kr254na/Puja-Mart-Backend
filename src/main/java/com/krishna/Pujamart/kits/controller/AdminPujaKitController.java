package com.krishna.Pujamart.kits.controller;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.kits.dto.PujaKitRequest;
import com.krishna.Pujamart.kits.dto.PujaKitResponse;
import com.krishna.Pujamart.kits.service.PujaKitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/kits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPujaKitController {

    private final PujaKitService pujaKitService;

    @PostMapping
    public ResponseEntity<ApiResponse<PujaKitResponse>> createKit(@Valid @RequestBody PujaKitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pujaKitService.createKit(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PujaKitResponse>> updateKit(
            @PathVariable UUID id,
            @Valid @RequestBody PujaKitRequest request) {
        return ResponseEntity.ok(pujaKitService.updateKit(id, request));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleKitStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(pujaKitService.toggleKitStatus(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKit(@PathVariable UUID id) {
        return ResponseEntity.ok(pujaKitService.deleteKit(id));
    }
}
