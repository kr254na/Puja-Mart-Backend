package com.krishna.Pujamart.catalog.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException (String message) {
        super(message);
    }
}
