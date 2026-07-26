package com.krishna.Pujamart.kits.service;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.kits.dto.PujaKitRequest;
import com.krishna.Pujamart.kits.dto.PujaKitResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PujaKitService {
    ApiResponse<Page<PujaKitResponse>> getFilteredKits(UUID deityId, Pageable pageable);
    ApiResponse<PujaKitResponse> getKitById(UUID id);
    ApiResponse<PujaKitResponse> createKit(PujaKitRequest request);
    ApiResponse<PujaKitResponse> updateKit(UUID id, PujaKitRequest request);
    ApiResponse<Void> toggleKitStatus(UUID id);
    ApiResponse<Void> deleteKit(UUID id);
}
