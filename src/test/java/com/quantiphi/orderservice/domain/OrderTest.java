package com.quantiphi.orderservice.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void addItem_CalculatesTotalCorrectly() {
        // Arrange
        Order order = new Order("customer-123");
        OrderItem item1 = new OrderItem("A1", 2, BigDecimal.valueOf(10.0));
        OrderItem item2 = new OrderItem("B2", 1, BigDecimal.valueOf(20.0));

        // Act
        order.addItem(item1);
        order.addItem(item2);

        // Assert
        assertEquals(new BigDecimal("40.00"), order.getTotalPrice());
        assertEquals(2, order.getItems().size());
    }

    @Test
    void updateStatus_ValidTransition_UpdatesSuccessfully() {
        // Arrange
        Order order = new Order("customer-123");

        // Act
        order.updateStatus(OrderStatus.PROCESSING);

        // Assert
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
    }

    @Test
    void updateStatus_InvalidTransition_ThrowsException() {
        // Arrange
        Order order = new Order("customer-123");
        order.updateStatus(OrderStatus.PROCESSING);
        order.updateStatus(OrderStatus.COMPLETED);

        // Act & Assert
        assertThrows(IllegalStateException.class, 
            () -> order.updateStatus(OrderStatus.PROCESSING));
    }

    @Test
    void orderStatusTransitions_FromCreated_AllowsProcessingAndCancelled() {
        // Arrange & Act & Assert
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.PROCESSING));
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.CREATED.canTransitionTo(OrderStatus.COMPLETED));
        assertFalse(OrderStatus.CREATED.canTransitionTo(OrderStatus.CREATED));
    }

    @Test
    void orderStatusTransitions_FromProcessing_AllowsCompletedAndCancelled() {
        // Arrange & Act & Assert
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.COMPLETED));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.CREATED));
        assertFalse(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.PROCESSING));
    }

    @Test
    void orderStatusTransitions_FromCompleted_AllowsNoTransitions() {
        // Arrange & Act & Assert
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.CREATED));
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.PROCESSING));
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void newOrder_HasCorrectInitialState() {
        // Act
        Order order = new Order("customer-123");

        // Assert
        assertEquals("customer-123", order.getCustomerId());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(BigDecimal.ZERO, order.getTotalPrice());
        assertTrue(order.getItems().isEmpty());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }
}
