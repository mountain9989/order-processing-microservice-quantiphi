package com.quantiphi.orderservice.domain;

/**
 * Enumeration of possible order statuses with state transition validation.
 * Implements a state machine pattern to ensure valid status transitions.
 */
public enum OrderStatus {
    /** Initial state when order is created */
    CREATED,
    
    /** Order is being processed */
    PROCESSING,
    
    /** Order has been completed successfully */
    COMPLETED,
    
    /** Order has been cancelled */
    CANCELLED;

    /**
     * Validates if a transition to the new status is allowed.
     *
     * @param newStatus the target status
     * @return true if transition is allowed, false otherwise
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case CREATED -> newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING -> newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
