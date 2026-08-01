package com.krishna.Pujamart.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressDto shippingAddress;
}
