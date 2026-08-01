package com.krishna.Pujamart.order.utility;

import com.krishna.Pujamart.order.dto.AddressDto;
import com.krishna.Pujamart.order.dto.OrderItemResponse;
import com.krishna.Pujamart.order.dto.OrderResponse;
import com.krishna.Pujamart.order.model.Order;
import com.krishna.Pujamart.order.model.OrderItem;
import com.krishna.Pujamart.order.model.ShippingAddress;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toOrderResponse(Order order);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    ShippingAddress toShippingAddress(AddressDto addressDto);

    AddressDto toAddressDto(ShippingAddress shippingAddress);
}
