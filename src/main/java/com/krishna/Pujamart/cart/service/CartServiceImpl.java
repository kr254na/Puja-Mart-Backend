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
import java.util.*;

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
        warmUpCartKitsCache(cart);
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
            kit = pujaKitRepository.findActiveByIdWithDetails(request.getKitId())
                    .orElseThrow(() -> new PujaKitNotFoundException("PujaKit not found or is inactive with ID: " + request.getKitId()));

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
            if (!product.getActive()) {
                throw new ProductNotFoundException("Product not found with ID: " + request.getProductId());
            }

            if (request.getVariantId() != null) {
                variant = productVariantRepository.findById(request.getVariantId())
                        .orElseThrow(() -> new ProductVariantNotFoundException("Variant not found with ID: " + request.getVariantId()));
                if (!variant.getActive()) {
                    throw new ProductVariantNotFoundException("Variant not found with ID: " + request.getVariantId());
                }
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
        int finalQuantity = request.getQuantity();
        if (existingItemOpt.isPresent()) {
            finalQuantity += existingItemOpt.get().getQuantity();
        }

        // Validate stock with the final cumulative quantity
        if (kit != null) {
            validateKitStockForCart(kit, finalQuantity);
        } else if (variant != null) {
            if (variant.getStockQuantity() < finalQuantity) {
                throw new InvalidCartOperationException(
                        "Insufficient stock for variant: " + variant.getSku() +
                                " (Available: " + variant.getStockQuantity() + ")"
                );
            }
        } else if (product != null) {
            if (product.getStockQuantity() < finalQuantity) {
                throw new InvalidCartOperationException(
                        "Insufficient stock for product: " + product.getName() +
                                " (Available: " + product.getStockQuantity() + ")"
                );
            }
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(finalQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(variant)
                    .kit(kit)
                    .quantity(finalQuantity)
                    .build();
            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        warmUpCartKitsCache(savedCart);
        return ApiResponse.success("Item added to cart successfully", cartMapper.toCartResponse(savedCart));
    }

    @Override
    public ApiResponse<CartResponse> updateItemQuantity(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCartEntity(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with ID: " + itemId));

        int newQuantity = request.getQuantity();

        // 1. Check stock based on the item type (Kit, Variant, or Product)
        if (item.getKit() != null) {
            // Check stock of every item inside the Puja Kit for the new quantity
            for (PujaKitItem kitItem : item.getKit().getItems()) {
                int requiredStock = kitItem.getDefaultQuantity() * newQuantity;

                if (kitItem.getVariant() != null) {
                    if (kitItem.getVariant().getStockQuantity() < requiredStock) {
                        throw new InvalidCartOperationException(
                                "Insufficient stock for kit item: " + kitItem.getVariant().getSku() +
                                        " (Available: " + kitItem.getVariant().getStockQuantity() + ")"
                        );
                    }
                } else if (kitItem.getProduct() != null) {
                    if (kitItem.getProduct().getStockQuantity() < requiredStock) {
                        throw new InvalidCartOperationException(
                                "Insufficient stock for kit item: " + kitItem.getProduct().getName() +
                                        " (Available: " + kitItem.getProduct().getStockQuantity() + ")"
                        );
                    }
                }
            }
        } else if (item.getVariant() != null) {
            // Check stock of the product variant
            if (item.getVariant().getStockQuantity() < newQuantity) {
                throw new InvalidCartOperationException(
                        "Insufficient stock for variant: " + item.getVariant().getSku() +
                                " (Available: " + item.getVariant().getStockQuantity() + ")"
                );
            }
        } else if (item.getProduct() != null) {
            // Check stock of the base product
            if (item.getProduct().getStockQuantity() < newQuantity) {
                throw new InvalidCartOperationException(
                        "Insufficient stock for product: " + item.getProduct().getName() +
                                " (Available: " + item.getProduct().getStockQuantity() + ")"
                );
            }
        }

        // 2. If validation passes, update quantity and save
        item.setQuantity(newQuantity);
        Cart savedCart = cartRepository.save(cart);
        warmUpCartKitsCache(savedCart);

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
        warmUpCartKitsCache(savedCart);

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

    private void warmUpCartKitsCache(Cart cart) {
        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            List<UUID> kitIds = cart.getItems().stream()
                    .map(CartItem::getKit)
                    .filter(Objects::nonNull)
                    .map(PujaKit::getId)
                    .toList();
            if (!kitIds.isEmpty()) {
                pujaKitRepository.findAllWithItemsAndDetailsByIds(kitIds);
            }
        }
    }

    private void validateAddToCartRequest(AddToCartRequest request) {
        if (request.getKitId() == null && request.getProductId() == null) {
            throw new InvalidCartOperationException("Either a Product ID or a Puja Kit ID must be provided.");
        }
        if (request.getKitId() != null && request.getProductId() != null) {
            throw new InvalidCartOperationException("Cannot add both a Puja Kit and a individual Product in a single item request.");
        }

    }

    private void validateKitStockForCart(PujaKit kit, int cartQuantity) {
        for (PujaKitItem kitItem : kit.getItems()) {
            int requiredStock = kitItem.getDefaultQuantity() * cartQuantity;
            if (kitItem.getVariant() != null) {
                if (kitItem.getVariant().getStockQuantity() < requiredStock) {
                    throw new InvalidCartOperationException("Not enough stock for kit item: " + kitItem.getVariant().getSku());
                }
            } else if (kitItem.getProduct() != null) {
                if (kitItem.getProduct().getStockQuantity() < requiredStock) {
                    throw new InvalidCartOperationException("Not enough stock for kit item: " + kitItem.getProduct().getName());
                }
            }
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
