package com.krishna.Pujamart.cart.service;

import com.krishna.Pujamart.cart.dto.*;
import com.krishna.Pujamart.cart.exception.*;
import com.krishna.Pujamart.cart.model.Cart;
import com.krishna.Pujamart.cart.model.CartItem;
import com.krishna.Pujamart.cart.repository.CartRepository;
import com.krishna.Pujamart.cart.utility.CartMapper;
import com.krishna.Pujamart.catalog.exception.ProductNotFoundException;
import com.krishna.Pujamart.catalog.exception.ProductVariantNotFoundException;
import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import com.krishna.Pujamart.catalog.repository.ProductRepository;
import com.krishna.Pujamart.catalog.repository.ProductVariantRepository;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.kits.exception.ProductVariantMismatchException;
import com.krishna.Pujamart.kits.exception.PujaKitNotFoundException;
import com.krishna.Pujamart.kits.model.PujaKit;
import com.krishna.Pujamart.kits.model.PujaKitItem;
import com.krishna.Pujamart.kits.repository.PujaKitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PujaKitRepository pujaKitRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CartResponse> getCart(UUID userId) {
        Cart cart = getOrCreateCartEntity(userId);
        return ApiResponse.success("Cart retrieved successfully", cartMapper.toCartResponse(cart));
    }

    @Override
    public ApiResponse<CartResponse> addItemToCart(UUID userId, AddToCartRequest request) {
        validateAddToCartRequest(request);

        Cart cart = getOrCreateCartEntity(userId);

        Product product = null;
        ProductVariant variant = null;
        PujaKit kit = null;

        if (request.getKitId() != null) {
            kit = pujaKitRepository.findById(request.getKitId())
                    .orElseThrow(() -> new PujaKitNotFoundException("PujaKit not found with ID: " + request.getKitId()));
            if (!Boolean.TRUE.equals(kit.getActive())) {
                throw new InvalidCartOperationException("Selected Puja Kit is currently unavailable.");
            }

            // Validation A: Ensure Puja Kit itself has starting prices
            if (kit.getBasePrice() == null && kit.getDiscountPrice() == null) {
                throw new InvalidCartOperationException("This Puja Kit is for inquiry only and cannot be added to the cart.");
            }
            // Validation B: Ensure no nested product in the kit has a null price
            for (PujaKitItem item : kit.getItems()) {
                if (item.getEffectivePrice() == null) {
                    throw new InvalidCartOperationException("Cannot add this Puja Kit to the cart because it contains products without prices.");
                }
            }
        } else {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + request.getProductId()));

            if (request.getVariantId() != null) {
                variant = productVariantRepository.findById(request.getVariantId())
                        .orElseThrow(() -> new ProductVariantNotFoundException("Variant not found with ID: " + request.getVariantId()));
                if (variant != null && !variant.getProduct().getId().equals(product.getId())) {
                    throw new ProductVariantMismatchException("The selected variant does not belong to the specified product.");
                }

                // 2. Validation: Ensure the variant (or its parent product fallback) has a price
                BigDecimal variantPrice = (variant.getDiscountPriceOverride() != null)
                        ? variant.getDiscountPriceOverride()
                        : variant.getBasePriceOverride();

                if (variantPrice == null) {
                    // Fall back to check the parent product's price
                    BigDecimal parentPrice = (product.getDiscountPrice() != null)
                            ? product.getDiscountPrice()
                            : product.getPrice();

                    if (parentPrice == null) {
                        throw new InvalidCartOperationException("This variant is for inquiry only and cannot be added to the cart.");
                    }
                }
            } else {
                // 3. Validation: Ensure the base product has a price
                if (product.getPrice() == null && product.getDiscountPrice() == null) {
                    throw new InvalidCartOperationException("This product is for inquiry only and cannot be added to the cart.");
                }
            }
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItemOpt = findMatchingCartItem(cart, request);

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(variant)
                    .kit(kit)
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return ApiResponse.success("Item added to cart successfully", cartMapper.toCartResponse(savedCart));
    }

    @Override
    public ApiResponse<CartResponse> updateItemQuantity(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCartEntity(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with ID: " + itemId));

        item.setQuantity(request.getQuantity());
        Cart savedCart = cartRepository.save(cart);

        return ApiResponse.success("Cart item quantity updated successfully", cartMapper.toCartResponse(savedCart));
    }

    @Override
    public ApiResponse<CartResponse> removeItemFromCart(UUID userId, UUID itemId) {
        Cart cart = getOrCreateCartEntity(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with ID: " + itemId));

        cart.removeItem(item);
        Cart savedCart = cartRepository.save(cart);

        return ApiResponse.success("Item removed from cart successfully", cartMapper.toCartResponse(savedCart));
    }

    @Override
    public ApiResponse<Void> clearCart(UUID userId) {
        Cart cart = getOrCreateCartEntity(userId);
        cart.getItems().clear();
        cartRepository.save(cart);

        return ApiResponse.success("Cart cleared successfully");
    }

    private Cart getOrCreateCartEntity(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build()));
    }

    private void validateAddToCartRequest(AddToCartRequest request) {
        if (request.getKitId() == null && request.getProductId() == null) {
            throw new InvalidCartOperationException("Either a Product ID or a Puja Kit ID must be provided.");
        }
        if (request.getKitId() != null && request.getProductId() != null) {
            throw new InvalidCartOperationException("Cannot add both a Puja Kit and a individual Product in a single item request.");
        }

    }

    private Optional<CartItem> findMatchingCartItem(Cart cart, AddToCartRequest request) {
        return cart.getItems().stream().filter(item -> {
            if (request.getKitId() != null) {
                return item.getKit() != null && item.getKit().getId().equals(request.getKitId());
            } else {
                boolean productMatch = item.getProduct() != null && item.getProduct().getId().equals(request.getProductId());
                boolean variantMatch = Objects.equals(
                        item.getVariant() != null ? item.getVariant().getId() : null,
                        request.getVariantId()
                );
                return productMatch && variantMatch;
            }
        }).findFirst();
    }
}
