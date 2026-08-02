package com.krishna.Pujamart.order.service;

import com.krishna.Pujamart.order.dto.ShiprocketLoginResponse;
import com.krishna.Pujamart.order.dto.ShiprocketRateResponse;
import com.krishna.Pujamart.order.exception.ThirdPartyServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShiprocketServiceImpl implements ShippingService {

    private final RestTemplate restTemplate;
    private static final String LOGIN_URL =
            "https://apiv2.shiprocket.in/v1/external/auth/login";
    private static final String RATE_URL =
            "https://apiv2.shiprocket.in/v1/external/courier/serviceability";
    private static final Duration TOKEN_VALIDITY =
            Duration.ofDays(9);

    @Value("${shiprocket.api.email}")
    private String email;

    @Value("${shiprocket.api.password}")
    private String password;

    @Value("${shiprocket.warehouse.postal-code}")
    private String pickupPostcode;

    private volatile String cachedToken;
    private volatile Instant tokenExpiry;

    // Authenticates and gets a token (Cached in-memory)
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

            // Build query parameters
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
}

