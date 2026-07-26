package com.krishna.Pujamart.kits.controller;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.kits.dto.PujaKitResponse;
import com.krishna.Pujamart.kits.service.PujaKitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/kits")
@RequiredArgsConstructor
public class PujaKitController {

    private final PujaKitService pujaKitService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PujaKitResponse>>> getAllKits(
            @RequestParam(required = false) UUID deityId,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(pujaKitService.getFilteredKits(deityId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PujaKitResponse>> getKitById(@PathVariable UUID id) {
        return ResponseEntity.ok(pujaKitService.getKitById(id));
    }

}
