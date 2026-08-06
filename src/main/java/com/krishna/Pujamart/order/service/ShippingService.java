package com.krishna.Pujamart.order.service;

import com.krishna.Pujamart.order.model.Order;

import java.math.BigDecimal;

public interface ShippingService {
    BigDecimal calculateShippingRate(String deliveryPostcode, BigDecimal totalWeightKg, boolean isCod, BigDecimal declaredValue);
    void createShipment(Order order);
}
