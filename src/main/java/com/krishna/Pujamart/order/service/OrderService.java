package com.krishna.Pujamart.order.service;


import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.order.dto.CreateOrderRequest;
import com.krishna.Pujamart.order.dto.OrderResponse;
import com.krishna.Pujamart.order.dto.UpdateOrderStatusRequest;
import com.krishna.Pujamart.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    ApiResponse<OrderResponse> createOrderFromCart(UUID userId, CreateOrderRequest request);
    ApiResponse<OrderResponse> getOrderById(UUID userId, UUID orderId);
    ApiResponse<Page<OrderResponse>> getUserOrders(UUID userId, Pageable pageable);
    ApiResponse<OrderResponse> cancelOrder(UUID userId, UUID orderId);

    // Admin APIs
    ApiResponse<OrderResponse> updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request);

    ApiResponse<Page<OrderResponse>> getAllOrdersForAdmin(OrderStatus status, Pageable pageable);
}
