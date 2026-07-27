package com.krishna.Pujamart.cart.controller;

import com.krishna.Pujamart.cart.dto.AddToCartRequest;
import com.krishna.Pujamart.cart.dto.CartResponse;
import com.krishna.Pujamart.cart.dto.UpdateCartItemRequest;
import com.krishna.Pujamart.cart.service.CartService;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.identity.utility.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(cartService.getCart(userPrincipal.getUser().getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(userPrincipal.getUser().getId(), request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userPrincipal.getUser().getId(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(userPrincipal.getUser().getId(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(cartService.clearCart(userPrincipal.getUser().getId()));
    }
}
