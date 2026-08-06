package com.krishna.Pujamart.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShiprocketOrderResponse {

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("shipment_id")
    private Long shipmentId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("status_code")
    private int statusCode;

    @JsonProperty("awb_code")
    private String awbCode;

    @JsonProperty("courier_name")
    private String courierName;
}
