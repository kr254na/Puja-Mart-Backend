package com.krishna.Pujamart.catalog.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException (String message) {
        super(message);
    }
}