package com.krishna.Pujamart.identity.controller;

import com.krishna.Pujamart.identity.dto.*;
import com.krishna.Pujamart.identity.service.UserService;
import com.krishna.Pujamart.identity.utility.UserPrincipal;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(userService.getProfile(userPrincipal.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        return ResponseEntity.ok(userService.updateProfile(userPrincipal.getUsername(), profileUpdateRequest));
    }

    @PatchMapping("/me/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        return ResponseEntity.ok(userService.changePassword(userPrincipal.getUsername(), changePasswordRequest));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(userService.deleteAccount(userPrincipal.getUsername()));
    }
}