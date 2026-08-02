package com.krishna.Pujamart.order.service;

import java.math.BigDecimal;

public interface ShippingService {
    BigDecimal calculateShippingRate(String deliveryPostcode, BigDecimal totalWeightKg, boolean isCod, BigDecimal declaredValue);
}
