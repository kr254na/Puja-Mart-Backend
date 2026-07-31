package com.krishna.Pujamart.wishlist.controller;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.identity.utility.UserPrincipal;
import com.krishna.Pujamart.wishlist.dto.AddToWishlistRequest;
import com.krishna.Pujamart.wishlist.dto.WishlistResponse;
import com.krishna.Pujamart.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(wishlistService.getWishlistByUserId(userPrincipal.getUser().getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<WishlistResponse>> addItem(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AddToWishlistRequest request) {
        return ResponseEntity.ok(wishlistService.addItemToWishlist(userPrincipal.getUser().getId(), request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeItem(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(wishlistService.removeItemFromWishlist(userPrincipal.getUser().getId(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearWishlist(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(wishlistService.clearWishlist(userPrincipal.getUser().getId()));
    }
}
