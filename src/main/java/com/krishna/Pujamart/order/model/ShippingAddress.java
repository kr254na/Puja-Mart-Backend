package com.krishna.Pujamart.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {

    @Column(name = "shipping_recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "shipping_phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "shipping_street_address", nullable = false)
    private String streetAddress;

    @Column(name = "shipping_city", nullable = false)
    private String city;

    @Column(name = "shipping_state", nullable = false)
    private String state;

    @Column(name = "shipping_postal_code", nullable = false, length = 10)
    private String postalCode;

    @Column(name = "shipping_country", nullable = false, length = 50)
    private String country;
}