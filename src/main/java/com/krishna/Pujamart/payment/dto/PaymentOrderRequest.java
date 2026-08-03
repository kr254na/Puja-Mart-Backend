package com.krishna.Pujamart.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentOrderRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;
}