package com.krishna.Pujamart.order.dto;

import com.krishna.Pujamart.order.enums.OrderStatus;
import com.krishna.Pujamart.order.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private AddressDto shippingAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime placedAt;
}