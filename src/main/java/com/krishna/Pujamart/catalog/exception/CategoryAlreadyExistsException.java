package com.krishna.Pujamart.catalog.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException (String message) {
        super(message);
    }
}
