package com.krishna.Pujamart.payment.model;

import com.krishna.Pujamart.order.model.Order;
import com.krishna.Pujamart.order.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id"),
        @Index(name = "idx_payments_gateway_order", columnList = "gateway_order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "gateway", nullable = false, length = 32)
    private String gateway; // e.g., "RAZORPAY", "COD"

    @Column(name = "gateway_order_id", length = 64)
    private String gatewayOrderId; // Razorpay Order ID

    @Column(name = "gateway_payment_id", length = 64)
    private String gatewayPaymentId; // Razorpay Payment ID

    @Column(name = "gateway_signature", length = 255)
    private String gatewaySignature; // Razorpay Signature

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
