package com.quantiphi.orderservice.exception;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}
