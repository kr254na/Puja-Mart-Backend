package com.krishna.Pujamart.catalog.exception;

public class ProductVariantNotFoundException extends RuntimeException {
    public ProductVariantNotFoundException(String message) {
        super(message);
    }
}

