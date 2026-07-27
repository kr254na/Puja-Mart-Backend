package com.krishna.Pujamart.cart.service;

import com.krishna.Pujamart.cart.dto.AddToCartRequest;
import com.krishna.Pujamart.cart.dto.CartResponse;
import com.krishna.Pujamart.cart.dto.UpdateCartItemRequest;
import com.krishna.Pujamart.identity.dto.ApiResponse;

import java.util.UUID;

public interface CartService {
    ApiResponse<CartResponse> getCart(UUID userId);
    ApiResponse<CartResponse> addItemToCart(UUID userId, AddToCartRequest request);
    ApiResponse<CartResponse> updateItemQuantity(UUID userId, UUID itemId, UpdateCartItemRequest request);
    ApiResponse<CartResponse> removeItemFromCart(UUID userId, UUID itemId);
    ApiResponse<Void> clearCart(UUID userId);
}
