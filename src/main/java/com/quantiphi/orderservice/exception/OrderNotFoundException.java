package com.quantiphi.orderservice.exception;

public class OrderNotFoundException extends RuntimeException {
    
    public OrderNotFoundException(Long orderId) {
        super(String.format("Order with ID %d not found", orderId));
    }
}
