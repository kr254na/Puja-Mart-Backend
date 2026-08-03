package com.krishna.Pujamart.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class PaymentOrderResponse {
    private UUID orderId;
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;

    // Merchant & Display Info (for Razorpay Standard Checkout modal)
    private String name;
    private String description;
    private String image;

    // Customer Info
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Key-value pairs for Razorpay custom metadata/notes
    private Map<String, String> notes;
}