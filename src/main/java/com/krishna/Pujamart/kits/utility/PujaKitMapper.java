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
}

