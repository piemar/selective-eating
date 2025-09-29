package com.example.app.exceptions;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String resourceType, String field, String value) {
        super(String.format("%s already exists with %s: %s", resourceType, field, value));
    }
}
