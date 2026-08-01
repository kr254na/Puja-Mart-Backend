package com.krishna.Pujamart.order.controller;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.identity.utility.UserPrincipal;
import com.krishna.Pujamart.order.dto.CreateOrderRequest;
import com.krishna.Pujamart.order.dto.OrderResponse;
import com.krishna.Pujamart.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrderFromCart(userPrincipal.getUser().getId(), request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(userPrincipal.getUser().getId(), orderId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(orderService.getUserOrders(userPrincipal.getUser().getId(), pageable));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(userPrincipal.getUser().getId(), orderId));
    }
}