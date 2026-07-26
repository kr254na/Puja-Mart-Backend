package com.krishna.Pujamart.catalog.utility;

import com.krishna.Pujamart.catalog.dto.CategoryResponse;
import com.krishna.Pujamart.catalog.dto.DeityResponse;
import com.krishna.Pujamart.catalog.dto.ProductResponse;
import com.krishna.Pujamart.catalog.dto.ProductVariantResponse;
import com.krishna.Pujamart.catalog.model.Category;
import com.krishna.Pujamart.catalog.model.Deity;
import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "deityId", source = "deity.id")
    @Mapping(target = "deityName", source = "deity.name")
    ProductResponse toProductResponse(Product product);

    ProductVariantResponse toVariantResponse(ProductVariant variant);

    CategoryResponse toCategoryResponse(Category category);

    DeityResponse toDeityResponse(Deity deity);
}

