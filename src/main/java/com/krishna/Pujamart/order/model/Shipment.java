package com.krishna.Pujamart.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipments_order_id", columnList = "order_id"),
        @Index(name = "idx_shipments_shiprocket_order_id", columnList = "shiprocket_order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "shiprocket_order_id")
    private Long shiprocketOrderId;

    @Column(name = "shiprocket_shipment_id")
    private Long shiprocketShipmentId;

    @Column(name = "awb_code")
    private String awbCode;

    @Column(name = "courier_name")
    private String courierName;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Column(name = "pickup_status")
    private String pickupStatus;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
}
