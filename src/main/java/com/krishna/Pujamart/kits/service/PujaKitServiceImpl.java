package com.krishna.Pujamart.kits.service;

import com.krishna.Pujamart.cart.exception.InvalidCartOperationException;
import com.krishna.Pujamart.cart.repository.CartItemRepository;
import com.krishna.Pujamart.catalog.exception.DeityNotFoundException;
import com.krishna.Pujamart.catalog.exception.InvalidDiscountPriceException;
import com.krishna.Pujamart.catalog.exception.ProductNotFoundException;
import com.krishna.Pujamart.catalog.exception.ProductVariantNotFoundException;
import com.krishna.Pujamart.catalog.model.Deity;
import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import com.krishna.Pujamart.catalog.repository.DeityRepository;
import com.krishna.Pujamart.catalog.repository.ProductRepository;
import com.krishna.Pujamart.catalog.repository.ProductVariantRepository;
import com.krishna.Pujamart.kits.dto.PujaKitItemRequest;
import com.krishna.Pujamart.kits.dto.PujaKitRequest;
import com.krishna.Pujamart.kits.exception.*;
import com.krishna.Pujamart.kits.utility.PujaKitMapper;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.kits.dto.PujaKitResponse;
import com.krishna.Pujamart.kits.model.PujaKit;
import com.krishna.Pujamart.kits.model.PujaKitItem;
import com.krishna.Pujamart.kits.repository.PujaKitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PujaKitServiceImpl implements PujaKitService {

    private final PujaKitRepository pujaKitRepository;
    private final DeityRepository deityRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepository cartItemRepository;
    private final PujaKitMapper pujaKitMapper;

    @Override
    public ApiResponse<Page<PujaKitResponse>> getFilteredKits(UUID deityId, Pageable pageable) {
        return ApiResponse.success(
                "Kits fetched successfully",
                pujaKitRepository
                        .findFilteredKits(deityId, pageable)
                        .map(pujaKitMapper::toPujaKitResponse));
    }

    @Override
    public ApiResponse<PujaKitResponse> getKitById(UUID id) {
        PujaKit kit = pujaKitRepository.findActiveByIdWithDetails(id)
                .orElseThrow(() -> new PujaKitNotFoundException("PujaKit with Id:"+id+" not found"));

        return ApiResponse.success(
                "Kit fetched successfully",
                pujaKitMapper.toPujaKitResponse(kit));
    }

    @Override
    @Transactional
    public ApiResponse<PujaKitResponse> createKit(PujaKitRequest request) {
        if (request.getDiscountPrice() != null) {
            if (request.getBasePrice() == null) {
                throw new InvalidDiscountPriceException("Discount price cannot be set without a base price");
            }
            if (request.getBasePrice().compareTo(request.getDiscountPrice()) < 0) {
                throw new InvalidDiscountPriceException("Discount price cannot be greater than base price");
            }
        }


        if(pujaKitRepository.existsByNameIgnoreCase(request.getName())) {
            throw new PujaKitAlreadyExistsException("Puja Kit with name "+request.getName()+" already exists");
        }

        Deity deity = deityRepository.findById(request.getDeityId())
                .orElseThrow(() -> new DeityNotFoundException("Deity not found with ID: " + request.getDeityId()));

        PujaKit pujaKit = PujaKit.builder()
                .name(request.getName())
                .description(request.getDescription())
                .deity(deity)
                .imageUrls(request.getImageUrls())
                .active(request.getActive() != null ? request.getActive() : true)
                .basePrice(request.getBasePrice())
                .discountPrice(request.getDiscountPrice())
                .isCustomizable(request.getIsCustomizable() != null ? request.getIsCustomizable() : true)
                .items(new ArrayList<>())
                .build();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            Set<String> uniqueItems = new HashSet<>();
            for (PujaKitItemRequest item : request.getItems()) {
                String uniqueKey = item.getProductId() + "_" + (item.getVariantId() != null ? item.getVariantId() : "none");
                if (!uniqueItems.add(uniqueKey)) {
                    throw new ItemAlreadyExistsInKitException("Duplicate product/variant combination found in the kit items.");
                }
            }

            List<PujaKitItem> kitItems = request.getItems().stream().map(itemRequest -> {

                if (itemRequest.getMinQuantity() > itemRequest.getDefaultQuantity() ||
                        (itemRequest.getMaxQuantity() != null && itemRequest.getDefaultQuantity() > itemRequest.getMaxQuantity())) {
                    throw new InvalidQuantityException("Invalid quantity limits.");
                }

                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + itemRequest.getProductId()));

                ProductVariant variant = null;
                if (itemRequest.getVariantId() != null) {
                    variant = productVariantRepository.findById(itemRequest.getVariantId())
                            .orElseThrow(() -> new ProductVariantNotFoundException("Variant not found"));
                    if (!variant.getProduct().getId().equals(product.getId())) {
                        throw new ProductVariantMismatchException("Variant does not belong to the selected product.");
                    }
                }

                return PujaKitItem.builder()
                        .kit(pujaKit)
                        .product(product)
                        .variant(variant)
                        .defaultQuantity(itemRequest.getDefaultQuantity())
                        .minQuantity(itemRequest.getMinQuantity())
                        .maxQuantity(itemRequest.getMaxQuantity())
                        .isMandatory(itemRequest.getIsMandatory() != null ? itemRequest.getIsMandatory() : true)
                        .build();
            }).toList();

            pujaKit.getItems().addAll(kitItems);
        }

        PujaKit savedKit = pujaKitRepository.save(pujaKit);

        return ApiResponse.success(
                "Kit created successfully",
                pujaKitMapper.toPujaKitResponse(savedKit));
    }

    @Override
    @Transactional
    public ApiResponse<PujaKitResponse> updateKit(UUID id, PujaKitRequest request) {
        if (request.getDiscountPrice() != null) {
            if (request.getBasePrice() == null) {
                throw new InvalidDiscountPriceException("Discount price cannot be set without a base price");
            }
            if (request.getBasePrice().compareTo(request.getDiscountPrice()) < 0) {
                throw new InvalidDiscountPriceException("Discount price cannot be greater than base price");
            }
        }

        PujaKit existingKit = pujaKitRepository.findById(id)
                .orElseThrow(() -> new PujaKitNotFoundException("PujaKit not found with ID: " + id));


        if(pujaKitRepository.existsByNameIgnoreCaseAndIdNot(request.getName(),existingKit.getId())) {
            throw new PujaKitAlreadyExistsException("Puja Kit with name "+request.getName()+" already exists");
        }

        if (request.getBasePrice() == null) {
            if (cartItemRepository.existsByKitId(existingKit.getId())) {
                throw new InvalidCartOperationException("Cannot remove the price of this Puja Kit because it is currently in customer carts.");
            }
        }

        UUID currentDeityId = existingKit.getDeity() != null ? existingKit.getDeity().getId() : null;

        if (!Objects.equals(currentDeityId, request.getDeityId())) {
            if (request.getDeityId() != null) {
                Deity deity = deityRepository.findById(request.getDeityId())
                        .orElseThrow(() -> new DeityNotFoundException("Deity not found with ID: " + request.getDeityId()));
                existingKit.setDeity(deity);
            } else {
                existingKit.setDeity(null);
            }
        }

        existingKit.setName(request.getName());
        existingKit.setDescription(request.getDescription());
        existingKit.setImageUrls(request.getImageUrls());
        existingKit.setActive(request.getActive() != null ? request.getActive() : true);
        existingKit.setBasePrice(request.getBasePrice());
        existingKit.setDiscountPrice(request.getDiscountPrice());
        existingKit.setIsCustomizable(request.getIsCustomizable() != null ? request.getIsCustomizable() : true);

        existingKit.getItems().clear();
        pujaKitRepository.flush();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            Set<String> uniqueItems = new HashSet<>();
            for (PujaKitItemRequest item : request.getItems()) {
                String uniqueKey = item.getProductId() + "_" + (item.getVariantId() != null ? item.getVariantId() : "none");
                if (!uniqueItems.add(uniqueKey)) {
                    throw new ItemAlreadyExistsInKitException("Duplicate product/variant combination found in the kit items.");
                }
            }

            List<PujaKitItem> updatedItems = request.getItems().stream().map(itemRequest -> {

                if (itemRequest.getMinQuantity() > itemRequest.getDefaultQuantity() ||
                        (itemRequest.getMaxQuantity() != null && itemRequest.getDefaultQuantity() > itemRequest.getMaxQuantity())) {
                    throw new InvalidQuantityException("Invalid quantity limits.");
                }

                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + itemRequest.getProductId()));

                ProductVariant variant = null;
                if (itemRequest.getVariantId() != null) {
                    variant = productVariantRepository.findById(itemRequest.getVariantId())
                            .orElseThrow(() -> new ProductVariantNotFoundException("Variant not found"));
                    if (!variant.getProduct().getId().equals(product.getId())) {
                        throw new ProductVariantMismatchException("Variant does not belong to the selected product.");
                    }
                }

                return PujaKitItem.builder()
                        .kit(existingKit) // Link back to existing parent entity
                        .product(product)
                        .variant(variant)
                        .defaultQuantity(itemRequest.getDefaultQuantity())
                        .minQuantity(itemRequest.getMinQuantity())
                        .maxQuantity(itemRequest.getMaxQuantity())
                        .isMandatory(itemRequest.getIsMandatory() != null ? itemRequest.getIsMandatory() : true)
                        .build();
            }).toList();

            existingKit.getItems().addAll(updatedItems);
        }

        PujaKit savedKit = pujaKitRepository.save(existingKit);
        return ApiResponse.success(
                "Kit updated successfully",
                pujaKitMapper.toPujaKitResponse(savedKit));
    }

    @Override
    @Transactional
    public ApiResponse<Void> toggleKitStatus(UUID id) {
        PujaKit pujaKit = pujaKitRepository.findById(id)
                .orElseThrow(() -> new PujaKitNotFoundException("PujaKit not found with ID: " + id));

        boolean newStatus = !Boolean.TRUE.equals(pujaKit.getActive());
        pujaKit.setActive(newStatus);

        pujaKitRepository.save(pujaKit);

        String statusText = newStatus ? "activated" : "deactivated";
        return ApiResponse.success("PujaKit " + statusText + " successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteKit(UUID id) {
        if (!pujaKitRepository.existsById(id)) {
            throw new PujaKitNotFoundException("PujaKit not found with ID: " + id);
        }
        pujaKitRepository.deleteById(id);
        return ApiResponse.success("PujaKit permanently deleted successfully");
    }
}
