package com.krishna.Pujamart.kits.utility;

import com.krishna.Pujamart.catalog.utility.ProductMapper;
import com.krishna.Pujamart.kits.dto.PujaKitItemResponse;
import com.krishna.Pujamart.kits.dto.PujaKitResponse;
import com.krishna.Pujamart.kits.model.PujaKit;
import com.krishna.Pujamart.kits.model.PujaKitItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { ProductMapper.class })
public interface PujaKitMapper {

    @Mapping(target = "originalPrice", expression = "java(pujaKit.getActualPrice())")
    @Mapping(target = "hasMissingPrices", expression = "java(checkHasMissingPrices(pujaKit))")
    @Mapping(target = "inStock", expression = "java(checkInStock(pujaKit))")
    PujaKitResponse toPujaKitResponse(PujaKit pujaKit);

    @Mapping(target = "productResponse", source = "product")
    @Mapping(target = "productVariantResponse", source = "variant")
    PujaKitItemResponse toItemResponse(PujaKitItem item);

    // Helper: Flags if any item in the kit is currently priceless
    default Boolean checkHasMissingPrices(PujaKit kit) {
        if (kit == null || kit.getItems() == null) {
            return false;
        }
        for (PujaKitItem item : kit.getItems()) {
            if (item.getEffectivePrice() == null) {
                return true;
            }
        }
        return false;
    }

    // Helper: Checks if all mandatory items in the kit have sufficient stock
    default Boolean checkInStock(PujaKit kit) {
        if (kit == null || kit.getItems() == null || kit.getItems().isEmpty()) {
            return false;
        }
        for (PujaKitItem item : kit.getItems()) {
            if (Boolean.TRUE.equals(item.getIsMandatory())) {
                int required = item.getDefaultQuantity() != null ? item.getDefaultQuantity() : 1;
                if (item.getVariant() != null) {
                    if (item.getVariant().getStockQuantity() == null || item.getVariant().getStockQuantity() < required) {
                        return false;
                    }
                } else if (item.getProduct() != null) {
                    if (item.getProduct().getStockQuantity() == null || item.getProduct().getStockQuantity() < required) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}

