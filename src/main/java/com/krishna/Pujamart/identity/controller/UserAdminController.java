package com.krishna.Pujamart.identity.controller;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.identity.dto.UserResponse;
import com.krishna.Pujamart.identity.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(userAdminService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("id") UUID userId) {
        return ResponseEntity.ok(userAdminService.getUserById(userId));
    }

    @PatchMapping("/{id}/toggle-lock")
    public ResponseEntity<ApiResponse<String>> toggleUserLock(@PathVariable("id") UUID userId) {
        return ResponseEntity.ok(userAdminService.toggleUserStatus(userId));
    }
}