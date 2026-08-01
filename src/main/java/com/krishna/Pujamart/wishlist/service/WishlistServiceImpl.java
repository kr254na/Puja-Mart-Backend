package com.krishna.Pujamart.wishlist.service;

import com.krishna.Pujamart.catalog.exception.ProductNotFoundException;
import com.krishna.Pujamart.catalog.exception.ProductVariantNotFoundException;
import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import com.krishna.Pujamart.catalog.repository.ProductRepository;
import com.krishna.Pujamart.catalog.repository.ProductVariantRepository;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.kits.exception.PujaKitNotFoundException;
import com.krishna.Pujamart.kits.model.PujaKit;
import com.krishna.Pujamart.kits.repository.PujaKitRepository;
import com.krishna.Pujamart.wishlist.dto.AddToWishlistRequest;
import com.krishna.Pujamart.wishlist.dto.WishlistResponse;
import com.krishna.Pujamart.wishlist.exception.WishlistItemNotFoundException;
import com.krishna.Pujamart.wishlist.model.Wishlist;
import com.krishna.Pujamart.wishlist.model.WishlistItem;
import com.krishna.Pujamart.wishlist.repository.WishlistRepository;
import com.krishna.Pujamart.wishlist.utility.WishlistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PujaKitRepository pujaKitRepository;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<WishlistResponse> getWishlistByUserId(UUID userId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        // Pre-fetch nested details for any kits inside the wishlist
        List<UUID> kitIds = wishlist.getItems().stream()
                .map(WishlistItem::getKit)
                .filter(Objects::nonNull)
                .map(PujaKit::getId)
                .toList();
                if (!kitIds.isEmpty()) {
                        pujaKitRepository.findAllWithItemsAndDetailsByIds(kitIds);
                    }

        return ApiResponse.success(
                "Wishlist retrieved successfully",
                wishlistMapper.toWishlistResponse(wishlist));
    }


    @Override
    @Transactional
    public ApiResponse<WishlistResponse> addItemToWishlist(UUID userId, AddToWishlistRequest request) {
        Wishlist wishlist = getOrCreateWishlist(userId);

        boolean exists = wishlist.getItems().stream().anyMatch(item -> isDuplicate(item, request));
        if (exists) {
            return ApiResponse.success(
                    "Item already added to wishlist",
                    wishlistMapper.toWishlistResponse(wishlist));
        }

        WishlistItem newItem = WishlistItem.builder().build();

        if (request.getKitId() != null) {
            PujaKit kit = pujaKitRepository.findById(request.getKitId())
                    .orElseThrow(() -> new PujaKitNotFoundException("PujaKit with id "+request.getKitId()+" does not found"));
            newItem.setKit(kit);
        } else {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product with id "+request.getProductId()+" not found"));
            newItem.setProduct(product);

            if (request.getVariantId() != null) {
                ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                        .orElseThrow(() -> new ProductVariantNotFoundException("ProductVariant with id "+request.getVariantId()+" not found"));
                newItem.setVariant(variant);
            }
        }

        wishlist.addItem(newItem);
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        return ApiResponse.success(
                "Item added to wishlist successfully",
                wishlistMapper.toWishlistResponse(savedWishlist));
    }

    @Override
    @Transactional
    public ApiResponse<WishlistResponse> removeItemFromWishlist(
            UUID userId,
            UUID itemId) {

        Wishlist wishlist = getOrCreateWishlist(userId);

        boolean removed = wishlist.getItems()
                .removeIf(item -> item.getId().equals(itemId));

        if (!removed) {
            throw new WishlistItemNotFoundException(
                    "Wishlist item not found: " + itemId
            );
        }

        Wishlist updatedWishlist = wishlistRepository.save(wishlist);

        return ApiResponse.success(
                "Item removed from wishlist successfully",
                wishlistMapper.toWishlistResponse(updatedWishlist)
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> clearWishlist(UUID userId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);
        return ApiResponse.success("Wishlist cleared successfully");
    }

    private Wishlist getOrCreateWishlist(UUID userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> wishlistRepository.save(
                        Wishlist.builder()
                                .userId(userId)
                                .build()
                ));
    }

    private boolean isDuplicate(WishlistItem item, AddToWishlistRequest request) {
        if (request.getKitId() != null && item.getKit() != null) {
            return item.getKit().getId().equals(request.getKitId());
        }
        if (request.getProductId() != null && item.getProduct() != null && item.getProduct().getId().equals(request.getProductId())) {
            return Objects.equals(
                    item.getVariant() != null ? item.getVariant().getId() : null,
                    request.getVariantId()
            );
        }
        return false;
    }
}