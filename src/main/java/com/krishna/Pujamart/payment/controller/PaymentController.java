package com.krishna.Pujamart.payment.controller;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.identity.utility.UserPrincipal;
import com.krishna.Pujamart.payment.dto.PaymentOrderRequest;
import com.krishna.Pujamart.payment.dto.PaymentOrderResponse;
import com.krishna.Pujamart.payment.dto.PaymentVerificationRequest;
import com.krishna.Pujamart.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/razorpay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PaymentOrderRequest request) {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(userPrincipal.getUser().getId(), request));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyPayment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PaymentVerificationRequest request) {
        return ResponseEntity.ok(paymentService.verifyPaymentSignature(userPrincipal.getUser().getId(), request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        return ResponseEntity.ok(paymentService.processWebhookEvent(payload, signature));
    }
}