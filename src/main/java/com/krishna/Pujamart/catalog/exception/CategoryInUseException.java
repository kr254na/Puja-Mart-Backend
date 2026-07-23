package com.krishna.Pujamart.catalog.exception;

public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException (String message) {
        super(message);
    }
}
