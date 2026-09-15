package com.example1.demo3.exception;

public class ResourceNotFoundException extends IllegalArgumentException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
}
