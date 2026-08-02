package com.krishna.Pujamart.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ShiprocketRateResponse {
    private int status;
    private RateData data;

    @Data
    public static class RateData {
        @JsonProperty("available_courier_companies")
        private List<CourierCompany> availableCouriers;
    }

    @Data
    public static class CourierCompany {
        @JsonProperty("courier_name")
        private String courierName;
        private double rate;
        private String etd;
    }
}

