package com.krishna.Pujamart.order.service;

import com.krishna.Pujamart.identity.model.User;
import com.krishna.Pujamart.identity.repository.UserRepository;
import com.krishna.Pujamart.kits.model.PujaKitItem;
import com.krishna.Pujamart.order.dto.ShiprocketLoginResponse;
import com.krishna.Pujamart.order.dto.ShiprocketOrderRequest;
import com.krishna.Pujamart.order.dto.ShiprocketOrderResponse;
import com.krishna.Pujamart.order.dto.ShiprocketRateResponse;
import com.krishna.Pujamart.order.enums.PaymentStatus;
import com.krishna.Pujamart.order.exception.OrderNotFoundException;
import com.krishna.Pujamart.order.exception.ThirdPartyServiceException;
import com.krishna.Pujamart.order.model.Order;
import com.krishna.Pujamart.order.model.OrderItem;
import com.krishna.Pujamart.order.model.Shipment;
import com.krishna.Pujamart.order.repository.OrderRepository;
import com.krishna.Pujamart.order.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketServiceImpl implements ShippingService {

    private final RestTemplate restTemplate;
    private static final String LOGIN_URL =
            "https://apiv2.shiprocket.in/v1/external/auth/login";
    private static final String RATE_URL =
            "https://apiv2.shiprocket.in/v1/external/courier/serviceability";
    private static final Duration TOKEN_VALIDITY =
            Duration.ofDays(9);
    private static final String CREATE_ORDER_URL = "https://apiv2.shiprocket.in/v1/external/orders/create/adhoc";

    @Value("${shiprocket.api.email}")
    private String email;

    @Value("${shiprocket.api.password}")
    private String password;

    @Value("${shiprocket.warehouse.postal-code}")
    private String pickupPostcode;

    @Value("${shiprocket.pickup-location-name:Primary Warehouse}")
    private String pickupLocationName;
    @Value("${shiprocket.default.length:10}")
    private double defaultLength;
    @Value("${shiprocket.default.width:10}")
    private double defaultWidth;
    @Value("${shiprocket.default.height:10}")
    private double defaultHeight;

    private volatile String cachedToken;
    private volatile Instant tokenExpiry;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;

    public synchronized String getAuthToken() {

        if (cachedToken != null &&
                tokenExpiry != null &&
                Instant.now().isBefore(tokenExpiry.minusSeconds(60))) {

            return cachedToken;
        }

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);

        try {
            ResponseEntity<ShiprocketLoginResponse> response = restTemplate.postForEntity(
                    LOGIN_URL, request, ShiprocketLoginResponse.class
            );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {

                cachedToken = response.getBody().getToken();
                tokenExpiry = Instant.now().plus(TOKEN_VALIDITY);
                return cachedToken;
            }
        } catch (Exception e) {

            throw new ThirdPartyServiceException("Failed to authenticate with Shiprocket API");
        }
        throw new ThirdPartyServiceException("Invalid response from Shiprocket Authentication API");
    }

    @Override
    public BigDecimal calculateShippingRate(String deliveryPostcode, BigDecimal totalWeightKg, boolean isCod, BigDecimal declaredValue) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAuthToken());
            HttpEntity<?> entity = new HttpEntity<>(headers);


            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(RATE_URL)
                    .queryParam("pickup_postcode", pickupPostcode)
                    .queryParam("delivery_postcode", deliveryPostcode)
                    .queryParam("weight", totalWeightKg)
                    .queryParam("declared_value",declaredValue)
                    .queryParam("cod", isCod ? 1 : 0);
            ResponseEntity<ShiprocketRateResponse> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    ShiprocketRateResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getStatus() == 200) {
                ShiprocketRateResponse rateResponse = response.getBody();

                // Return the rate of the cheapest available courier
                return rateResponse.getData().getAvailableCouriers().stream()
                        .map(ShiprocketRateResponse.CourierCompany::getRate)
                        .min(Double::compare)
                        .map(BigDecimal::valueOf)
                        .orElseThrow(()-> new ThirdPartyServiceException("Failed to fetch the shipping charges"));
            }
            throw new ThirdPartyServiceException("Failed to fetch the shipping charges");
        }
        catch(Exception e) {
            throw new ThirdPartyServiceException("Failed to fetch shipping charges");
        }
    }

    @Async
    @Override
    @Transactional
    public void createShipment(Order order) {
        log.info("Starting asynchronous Shiprocket fulfillment for order: {}", order.getOrderNumber());
        try {

            Order activeOrder = orderRepository.findWithDetailsById(order.getId())
                    .orElseThrow(() -> new OrderNotFoundException("Order details not found for ID: " + order.getId()));


            String email = userRepository.findById(activeOrder.getUserId())
                    .map(User::getEmail)
                    .orElse(null);

            List<ShiprocketOrderRequest.OrderItemDto> items = activeOrder.getItems().stream()
                    .map(item -> ShiprocketOrderRequest.OrderItemDto.builder()
                            .name(item.getItemName())
                            .sku(item.getSku() != null ? item.getSku() : null)
                            .units(item.getQuantity())
                            .sellingPrice(item.getUnitPrice().doubleValue())
                            .build())
                    .toList();

            double packageWeight = calculateTotalWeight(activeOrder);

            String recipientName = activeOrder.getShippingAddress().getRecipientName();
            String firstName = recipientName;
            String lastName = "";
            int spaceIndex = recipientName.trim().indexOf(" ");
            if (spaceIndex > 0) {
                firstName = recipientName.substring(0, spaceIndex);
                lastName = recipientName.substring(spaceIndex + 1);
            }

            String orderDateStr = activeOrder.getPlacedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            ShiprocketOrderRequest shiprocketRequest = ShiprocketOrderRequest.builder()
                    .orderId(activeOrder.getOrderNumber())
                    .orderDate(orderDateStr)
                    .pickupLocation(pickupLocationName)
                    .billingCustomerName(firstName)
                    .billingLastName(lastName)
                    .billingAddress(activeOrder.getShippingAddress().getStreetAddress())
                    .billingCity(activeOrder.getShippingAddress().getCity())
                    .billingPincode(activeOrder.getShippingAddress().getPostalCode())
                    .billingState(activeOrder.getShippingAddress().getState())
                    .billingCountry(activeOrder.getShippingAddress().getCountry())
                    .billingEmail(email)
                    .billingPhone(activeOrder.getShippingAddress().getPhone())
                    .shippingIsBilling(true)
                    .orderItems(items)
                    .paymentMethod(activeOrder.getPaymentStatus() == PaymentStatus.PAID ? "Prepaid" : "COD")
                    .subTotal(activeOrder.getSubtotalAmount().doubleValue())
                    .length(defaultLength)
                    .width(defaultWidth)
                    .height(defaultHeight)
                    .weight(packageWeight)
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAuthToken());
            HttpEntity<ShiprocketOrderRequest> entity = new HttpEntity<>(shiprocketRequest, headers);
            ResponseEntity<ShiprocketOrderResponse> response = restTemplate.postForEntity(
                    CREATE_ORDER_URL,
                    entity,
                    ShiprocketOrderResponse.class
            );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ShiprocketOrderResponse body = response.getBody();
                if (body.getStatusCode() == 1 || body.getOrderId() != null) {
                    Shipment shipment = Shipment.builder()
                            .order(activeOrder)
                            .shiprocketOrderId(body.getOrderId())
                            .shiprocketShipmentId(body.getShipmentId())
                            .awbCode(body.getAwbCode())
                            .build();

                    activeOrder.setShipment(shipment);
                    orderRepository.save(activeOrder);

                    log.info("Shiprocket order created successfully. Order ID: {}, Shipment ID: {}", body.getOrderId(), body.getShipmentId());
                } else {
                    log.error("Shiprocket API returned error status. StatusCode: {}", body.getStatusCode());
                }
            } else {
                log.error("Failed to create Shiprocket shipment order. Response status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to execute Shiprocket automatic fulfillment for order: {}", order.getOrderNumber(), e);
        }
    }

    private double calculateTotalWeight(Order order) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            BigDecimal unitWeight = BigDecimal.ZERO;
            if (item.getKit() != null) {
                for (PujaKitItem kitItem : item.getKit().getItems()) {
                    BigDecimal itemUnitWeight = BigDecimal.ZERO;
                    if (kitItem.getVariant() != null && kitItem.getVariant().getWeight() != null) {
                        itemUnitWeight = kitItem.getVariant().getWeight();
                    } else if (kitItem.getProduct() != null && kitItem.getProduct().getWeight() != null) {
                        itemUnitWeight = kitItem.getProduct().getWeight();
                    }
                    unitWeight = unitWeight.add(itemUnitWeight.multiply(BigDecimal.valueOf(kitItem.getDefaultQuantity())));
                }
            } else if (item.getVariant() != null && item.getVariant().getWeight() != null) {
                unitWeight = item.getVariant().getWeight();
            } else if (item.getProduct() != null && item.getProduct().getWeight() != null) {
                unitWeight = item.getProduct().getWeight();
            }
            totalWeight = totalWeight.add(unitWeight.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return totalWeight.doubleValue();
    }
}

