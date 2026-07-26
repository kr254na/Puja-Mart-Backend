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

    PujaKitResponse toPujaKitResponse(PujaKit pujaKit);

    @Mapping(target = "productResponse", source = "product")
    @Mapping(target = "productVariantResponse", source = "variant")
    PujaKitItemResponse toItemResponse(PujaKitItem item);
}

