package com.krishna.Pujamart.payment.service;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.payment.dto.PaymentOrderRequest;
import com.krishna.Pujamart.payment.dto.PaymentOrderResponse;
import com.krishna.Pujamart.payment.dto.PaymentVerificationRequest;

import java.util.UUID;

public interface PaymentService {
    ApiResponse<PaymentOrderResponse> createRazorpayOrder(UUID userId, PaymentOrderRequest request);
    ApiResponse<String> verifyPaymentSignature(UUID userId, PaymentVerificationRequest request);
    ApiResponse<String> processWebhookEvent(String payload, String signature);
}
