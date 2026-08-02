package com.krishna.Pujamart.catalog.service;

import com.krishna.Pujamart.cart.exception.InvalidCartOperationException;
import com.krishna.Pujamart.cart.repository.CartItemRepository;
import com.krishna.Pujamart.catalog.dto.*;
import com.krishna.Pujamart.catalog.exception.*;
import com.krishna.Pujamart.catalog.model.*;
import com.krishna.Pujamart.catalog.repository.*;
import com.krishna.Pujamart.catalog.utility.ProductMapper;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DeityRepository deityRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductMapper productMapper;

    @Override
    public ApiResponse<Page<ProductResponse>> getFilteredProducts(UUID categoryId, UUID deityId, Pageable pageable) {
        Page<Product> productsPage = productRepository.findFilteredCatalog(categoryId, deityId, pageable);

        if (!productsPage.isEmpty()) {
            List<UUID> productIds = productsPage.getContent().stream().map(Product::getId).toList();
            productRepository.findAllWithVariantsByIds(productIds);
        }

        return ApiResponse.success("Products fetched successfully",
                productsPage.map(productMapper::toProductResponse));
    }

    @Override
    public ApiResponse<ProductResponse> getProductById(UUID id) {
        Product product = productRepository.findProductWithDetailsById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return ApiResponse.success("Product fetched successfully",
                productMapper.toProductResponse(product));
    }

    @Override
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.success("Categories fetched successfully",
                categoryRepository.findAll()
                        .stream()
                        .map(productMapper::toCategoryResponse)
                        .toList());
    }

    @Override
    public ApiResponse<List<DeityResponse>> getAllDeities() {
        return ApiResponse.success("Deities fetched successfully",
                deityRepository.findAll()
                .stream()
                .map(productMapper::toDeityResponse)
                .toList());
    }

    @Override
    @Transactional
    public ApiResponse<ProductResponse> createProduct(ProductRequest request) {

        if (request.getDiscountPrice() != null) {
            if (request.getPrice() == null) {
                throw new InvalidDiscountPriceException("Discount price cannot be set without a product price");
            }
            if (request.getDiscountPrice().compareTo(request.getPrice()) > 0) {
                throw new InvalidDiscountPriceException("Discount price cannot be greater than product price");
            }
        }


        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with ID: " + request.getCategoryId()
                        ));

        Deity deity = deityRepository.findById(request.getDeityId())
                .orElseThrow(() ->
                        new DeityNotFoundException(
                                "Deity not found with ID: " + request.getDeityId()
                        ));

        String baseSku = resolveOrGenerateSku(request.getSku(), request.getName(), null);

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(
                        request.getStockQuantity() != null
                                ? request.getStockQuantity()
                                : 0
                )
                .brand(request.getBrand())
                .sku(baseSku)
                .discountPrice(request.getDiscountPrice())
                .featured(
                        request.getFeatured() != null
                                ? request.getFeatured()
                                : false
                )
                .measurementUnit(request.getMeasurementUnit())
                .weight(request.getWeight())
                .imageUrls(
                        request.getImageUrls() != null
                                ? new ArrayList<>(request.getImageUrls())
                                : new ArrayList<>()
                )
                .category(category)
                .deity(deity)
                .variants(new ArrayList<>())
                .build();

        Product savedProduct = productRepository.save(product);

        return ApiResponse.success(
                "Product created successfully",
                productMapper.toProductResponse(savedProduct)
        );
    }

    @Override
    @Transactional
    public ApiResponse<ProductResponse> updateProduct(
            UUID id,
            ProductRequest request) {

        if (request.getDiscountPrice() != null) {
            if (request.getPrice() == null) {
                throw new InvalidDiscountPriceException("Discount price cannot be set without a product price");
            }
            if (request.getDiscountPrice().compareTo(request.getPrice()) > 0) {
                throw new InvalidDiscountPriceException("Discount price cannot be greater than product price");
            }
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with ID: " + id
                        ));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with ID: "
                                        + request.getCategoryId()
                        ));

        Deity deity = deityRepository.findById(request.getDeityId())
                .orElseThrow(() ->
                        new DeityNotFoundException(
                                "Deity not found with ID: "
                                        + request.getDeityId()
                        ));

        if (request.getPrice() == null) {
            // Check if the product itself is in a cart
            if (cartItemRepository.existsByProductId(product.getId())) {
                throw new InvalidCartOperationException("Cannot remove the price of this product because it is currently in customer carts.");
            }
            // Check if any variant relying on this product's price is in a cart
            if (cartItemRepository.existsPricelessVariantInCart(product.getId())) {
                throw new InvalidCartOperationException("Cannot remove the price of this product because its variants are in customer carts relying on this price.");
            }
        }

        String baseSku = resolveOrGenerateSku(request.getSku(), request.getName(), product.getId());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setSku(baseSku);
        product.setWeight(request.getWeight());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());

        product.setStockQuantity(
                request.getStockQuantity() != null
                        ? request.getStockQuantity()
                        : 0
        );

        product.setMeasurementUnit(request.getMeasurementUnit());

        product.setFeatured(
                request.getFeatured() != null
                        ? request.getFeatured()
                        : false
        );

        product.setCategory(category);
        product.setDeity(deity);

        product.getImageUrls().clear();

        if (request.getImageUrls() != null &&
                !request.getImageUrls().isEmpty()) {

            product.getImageUrls()
                    .addAll(request.getImageUrls());
        }

        Product updatedProduct =
                productRepository.save(product);

        return ApiResponse.success(
                "Product updated successfully",
                productMapper.toProductResponse(updatedProduct)
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
        return ApiResponse.success("Deleted the product successfully");
    }

    @Override
    @Transactional
    public ApiResponse<CategoryResponse> createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new CategoryAlreadyExistsException("Category with "+request.getName()+" already exists");
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return ApiResponse.success("Category created successfully",
                productMapper.toCategoryResponse(categoryRepository.save(category)));
    }

    @Override
    @Transactional
    public ApiResponse<DeityResponse> createDeity(DeityRequest request) {
        if (deityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DeityAlreadyExistsException("Deity with name "+request.getName()+" already exists");
        }
        Deity deity = Deity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return ApiResponse.success(
                "Deity created successfully",
                productMapper.toDeityResponse(deityRepository.save(deity)));
    }

    @Override
    @Transactional
    public ApiResponse<CategoryResponse> updateCategory(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException("Category not found"));
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(),id)) {
            throw new CategoryAlreadyExistsException("Category with name "+request.getName()+" already exists");
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        Category updatedCategory = categoryRepository.save(category);

        return ApiResponse.success(
                "Category updated successfully",
                productMapper.toCategoryResponse(updatedCategory));
    }

    @Override
    @Transactional
    public ApiResponse<DeityResponse> updateDeity(UUID id, DeityRequest request) {
        Deity deity = deityRepository.findById(id)
                .orElseThrow(() ->
                        new DeityNotFoundException("Deity not found"));
        if (deityRepository.existsByNameIgnoreCaseAndIdNot(request.getName(),id)) {
            throw new DeityAlreadyExistsException("Deity with name "+request.getName()+" already exists");
        }
        deity.setName(request.getName());
        deity.setDescription(request.getDescription());
        Deity updatedDeity = deityRepository.save(deity);
        return ApiResponse.success(
                "Deity updated successfully",
                productMapper.toDeityResponse(updatedDeity));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found");
        }
        if(productRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException("Category already in use");
        }
        categoryRepository.deleteById(id);
        return ApiResponse.success("Deleted the category successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteDeity(UUID id) {
        if (!deityRepository.existsById(id)) {
            throw new DeityNotFoundException("Deity not found");
        }
        if(productRepository.existsByDeityId(id)) {
            throw new DeityInUseException("Deity already in use");
        }
        deityRepository.deleteById(id);
        return ApiResponse.success("Deleted the deity successfully");
    }

    private String resolveOrGenerateSku(String requestedSku, String productTitle, UUID productId) {
        // 1. If manual SKU provided, validate and sanitize
        if (requestedSku != null && !requestedSku.isBlank()) {
            String cleanSku = requestedSku.trim().toUpperCase();

            boolean existsInProducts = (productId == null)
                    ? productRepository.existsBySku(cleanSku)
                    : productRepository.existsBySkuAndIdNot(cleanSku, productId);

            boolean existsInVariants = productVariantRepository.existsBySku(cleanSku);

            if (existsInProducts || existsInVariants) {
                throw new DuplicateSkuException("SKU already exists: " + cleanSku);
            }
            return cleanSku;
        }

        // 2. Auto-generate SKU fallback
        String prefix = "PJM";
        String titleSlug = productTitle.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        String titleCode = (titleSlug.length() >= 3) ? titleSlug.substring(0, 3) : "PRD";

        String generatedSku;
        boolean isTaken;

        do {
            String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            generatedSku = String.format("%s-%s-%s", prefix, titleCode, uniqueSuffix);

            boolean takenInProducts = (productId == null)
                    ? productRepository.existsBySku(generatedSku)
                    : productRepository.existsBySkuAndIdNot(generatedSku, productId);

            boolean takenInVariants = productVariantRepository.existsBySku(generatedSku);

            isTaken = takenInProducts || takenInVariants;

        } while (isTaken);

        return generatedSku;
    }
}
