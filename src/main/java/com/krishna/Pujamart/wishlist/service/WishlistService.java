package com.krishna.Pujamart.wishlist.service;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.wishlist.dto.AddToWishlistRequest;
import com.krishna.Pujamart.wishlist.dto.WishlistResponse;

import java.util.UUID;

public interface WishlistService {

    ApiResponse<WishlistResponse> getWishlistByUserId(UUID userId);

    ApiResponse<WishlistResponse> addItemToWishlist(UUID userId, AddToWishlistRequest request);

    ApiResponse<WishlistResponse> removeItemFromWishlist(UUID userId, UUID itemId);

    ApiResponse<Void> clearWishlist(UUID userId);
}