package com.krishna.Pujamart.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ShiprocketOrderRequest {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_date")
    private String orderDate;

    @JsonProperty("pickup_location")
    private String pickupLocation;

    @JsonProperty("billing_customer_name")
    private String billingCustomerName;

    @JsonProperty("billing_last_name")
    private String billingLastName;

    @JsonProperty("billing_address")
    private String billingAddress;

    @JsonProperty("billing_city")
    private String billingCity;

    @JsonProperty("billing_pincode")
    private String billingPincode;

    @JsonProperty("billing_state")
    private String billingState;

    @JsonProperty("billing_country")
    private String billingCountry;

    @JsonProperty("billing_email")
    private String billingEmail;

    @JsonProperty("billing_phone")
    private String billingPhone;

    @JsonProperty("shipping_is_billing")
    private boolean shippingIsBilling;

    @JsonProperty("order_items")
    private List<OrderItemDto> orderItems;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("sub_total")
    private double subTotal;

    @JsonProperty("length")
    private double length;

    @JsonProperty("width")
    private double width;

    @JsonProperty("height")
    private double height;

    @JsonProperty("weight")
    private double weight;

    @Data
    @Builder
    public static class OrderItemDto {
        private String name;
        private String sku;
        private int units;
        @JsonProperty("selling_price")
        private double sellingPrice;
    }
}
