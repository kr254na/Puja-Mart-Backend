package com.krishna.Pujamart.payment.service;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.identity.exception.UserNotFoundException;
import com.krishna.Pujamart.identity.repository.UserRepository;
import com.krishna.Pujamart.order.enums.OrderStatus;
import com.krishna.Pujamart.order.enums.PaymentStatus;
import com.krishna.Pujamart.order.exception.InvalidOrderStateException;
import com.krishna.Pujamart.order.exception.OrderNotFoundException;
import com.krishna.Pujamart.order.model.Order;
import com.krishna.Pujamart.order.repository.OrderRepository;
import com.krishna.Pujamart.payment.config.RazorpayConfig;
import com.krishna.Pujamart.payment.dto.PaymentOrderRequest;
import com.krishna.Pujamart.payment.dto.PaymentOrderResponse;
import com.krishna.Pujamart.payment.dto.PaymentVerificationRequest;
import com.krishna.Pujamart.payment.exception.PaymentGatewayException;
import com.krishna.Pujamart.payment.model.Payment;
import com.krishna.Pujamart.payment.repository.PaymentRepository;
import com.krishna.Pujamart.payment.utility.SignatureUtils;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RazorpayServiceImpl implements PaymentService {

    private final RazorpayConfig razorpayClient;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Value("${pujamart.store-name}")
    private String storeName;

    @Value("${pujamart.logo-url}")
    private String logoUrl;

    @Override
    @Transactional
    public ApiResponse<PaymentOrderResponse> createRazorpayOrder(UUID userId, PaymentOrderRequest request) {
        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + request.getOrderId()));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Cannot initialize payment for a cancelled order");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new InvalidOrderStateException("Order is already paid");
        }

        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderStateException("Invalid order amount");
        }

        String email = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"))
                .getEmail();

        Map<String, String> notesMap = Map.of(
                "pujamart_order_id", order.getId().toString(),
                "user_id", userId.toString()
        );

        Optional<Payment> existingPaymentOpt = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId());
        if (existingPaymentOpt.isPresent() && existingPaymentOpt.get().getGatewayOrderId() != null) {
            PaymentOrderResponse response = buildResponse(order, existingPaymentOpt.get().getGatewayOrderId(), email, notesMap);
            return ApiResponse.success("Existing Razorpay order found", response);
        }

        try {
            // Convert INR to Paisa
            long amountInPaisa = order.getTotalAmount()
                    .setScale(2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .longValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaisa);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", order.getOrderNumber());

            JSONObject notesJson = new JSONObject(notesMap);
            orderRequest.put("notes", notesJson);

            com.razorpay.Order razorpayOrder = razorpayClient.razorpayClient().orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            // Save a new record in the payments table
            Payment newPayment = Payment.builder()
                    .order(order)
                    .gateway("RAZORPAY")
                    .gatewayOrderId(razorpayOrderId)
                    .amount(order.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .build();

            paymentRepository.save(newPayment);

            PaymentOrderResponse response = buildResponse(order, razorpayOrderId, email, notesMap);

            return ApiResponse.success("Razorpay order created successfully", response);

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for orderId: {}", order.getId(), e);
            throw new PaymentGatewayException("Unable to initiate payment. Please try again.");
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> verifyPaymentSignature(UUID userId, PaymentVerificationRequest request) {
        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + request.getOrderId()));

        boolean isValid = SignatureUtils.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                keySecret
        );

        Payment payment = paymentRepository.findByGatewayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentGatewayException("Payment transaction details not found."));

        if (!isValid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Invalid payment signature.");
            paymentRepository.save(payment);
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            return ApiResponse.error("Payment verification failed. Invalid signature.");
        }

        confirmPaymentRecord(payment, request.getRazorpayPaymentId(), request.getRazorpaySignature());
        confirmOrderPayment(order);
        return ApiResponse.success("Payment verified successfully", "PAYMENT_CONFIRMED");

    }

    @Override
    @Transactional
    public ApiResponse<String> processWebhookEvent(String payload, String signature) {
        if (!SignatureUtils.verifyWebhookSignature(payload, signature, webhookSecret)) {
            log.warn("Invalid Razorpay webhook signature received");
            throw new PaymentGatewayException("Invalid webhook signature");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            String event = rootNode.path("event").asText();
            JsonNode paymentEntity = rootNode.path("payload").path("payment").path("entity");

            String razorpayOrderId = paymentEntity.path("order_id").asText();
            String razorpayPaymentId = paymentEntity.path("id").asText();

            Payment payment = paymentRepository.findByGatewayOrderId(razorpayOrderId).orElse(null);

            if (payment == null) {
                log.warn("Payment transaction not found for Razorpay Order ID in webhook: {}", razorpayOrderId);
                throw new PaymentGatewayException("Payment record not found");
            }

            Order order = payment.getOrder();

            if (order.getOrderStatus() == OrderStatus.CONFIRMED || payment.getStatus() == PaymentStatus.PAID) {
                log.info("Webhook duplicate received: Order {} is already confirmed.", order.getId());
                return ApiResponse.success("Event already processed", "ALREADY_PROCESSED");
            }
            if ("payment.captured".equals(event)) {
                if (payment.getStatus() != PaymentStatus.PAID) {
                    confirmPaymentRecord(payment, razorpayPaymentId, paymentEntity.path("signature").asText(null));
                    confirmOrderPayment(order);
                    log.info("Webhook: Payment success captured for Order {}", order.getId());
                }
            } else if ("payment.failed".equals(event)) {
                String errorDescription = paymentEntity.path("error_description").asText();
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage(errorDescription);
                paymentRepository.save(payment);
                order.setPaymentStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                log.info("Webhook: Payment marked as FAILED for Order {}", order.getId());
            }
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook", e);
            throw new PaymentGatewayException("Webhook processing error");
        }
        return ApiResponse.success("Payment verified successfully", "PAYMENT_CONFIRMED");
    }

    private void confirmPaymentRecord(Payment payment, String razorpayPaymentId, String signature) {
        payment.setGatewayPaymentId(razorpayPaymentId);
        payment.setGatewaySignature(signature);
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
    }

    private void confirmOrderPayment(Order order) {
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    private PaymentOrderResponse buildResponse(
            Order order,
            String razorpayOrderId,
            String email,
            Map<String, String> notes) {

        return PaymentOrderResponse.builder()
                .orderId(order.getId())
                .razorpayOrderId(razorpayOrderId)
                .amount(order.getTotalAmount())
                .currency(currency)
                .keyId(keyId)
                .name(storeName)
                .description("Order Payment #" + order.getId().toString().substring(0, 8))
                .image(logoUrl)
                .customerName(order.getShippingAddress().getRecipientName())
                .customerEmail(email)
                .customerPhone(order.getShippingAddress().getPhone())
                .notes(notes)
                .build();
    }
    @Override
    @Transactional
    public void refundPayment(Order order) {
        log.info("Initiating automatic Razorpay refund for order: {}", order.getOrderNumber());

        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId())
                .orElseThrow(() -> new PaymentGatewayException("Payment record not found for order ID: " + order.getId()));

        if (payment.getStatus() != PaymentStatus.PAID) {
            log.warn("Payment status is {}, cannot initiate refund.", payment.getStatus());
            return;
        }

        try {
            // Convert amount to Paisa
            long amountInPaisa = payment.getAmount()
                    .multiply(new BigDecimal("100"))
                    .longValue();

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amountInPaisa);
            refundRequest.put("speed", "optimum");

            // Request refund via Razorpay Client SDK
            com.razorpay.Refund refund = razorpayClient.razorpayClient().payments.refund(
                    payment.getGatewayPaymentId(),
                    refundRequest
            );

            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            log.info("Razorpay refund successful. Refund ID: {"+ refund.get("id")+"}");

        } catch (Exception e) {
            log.error("Razorpay refund API call failed for payment: {}", payment.getGatewayPaymentId(), e);
            throw new PaymentGatewayException("Refund processing failed: " + e.getMessage());
        }
    }
}
