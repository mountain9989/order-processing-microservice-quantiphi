package com.quantiphi.orderservice.service;

import com.quantiphi.orderservice.domain.Order;
import com.quantiphi.orderservice.domain.OrderStatus;
import com.quantiphi.orderservice.dto.CreateOrderRequest;
import com.quantiphi.orderservice.dto.OrderItemRequest;
import com.quantiphi.orderservice.dto.OrderResponse;
import com.quantiphi.orderservice.exception.InvalidOrderStatusTransitionException;
import com.quantiphi.orderservice.exception.OrderNotFoundException;
import com.quantiphi.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_ValidRequest_ReturnsOrderResponse() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
            "customer-123",
            List.of(
                new OrderItemRequest("A1", 2, BigDecimal.valueOf(10.0)),
                new OrderItemRequest("B2", 1, BigDecimal.valueOf(20.0))
            )
        );

        Order mockOrder = new Order("customer-123");
        mockOrder.addItem(new com.quantiphi.orderservice.domain.OrderItem("A1", 2, BigDecimal.valueOf(10.0)));
        mockOrder.addItem(new com.quantiphi.orderservice.domain.OrderItem("B2", 1, BigDecimal.valueOf(20.0)));

        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals("customer-123", response.getCustomerId());
        assertEquals(2, response.getItems().size());
        assertEquals(new BigDecimal("40.00"), response.getTotalPrice());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getOrder_ExistingOrder_ReturnsOrderResponse() {
        // Arrange
        Long orderId = 1L;
        Order mockOrder = new Order("customer-123");
        mockOrder.addItem(new com.quantiphi.orderservice.domain.OrderItem("A1", 2, BigDecimal.valueOf(10.0)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act
        OrderResponse response = orderService.getOrder(orderId);

        // Assert
        assertNotNull(response);
        assertEquals("customer-123", response.getCustomerId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrder_NonExistingOrder_ThrowsOrderNotFoundException() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(orderId));
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void updateOrderStatus_ValidTransition_UpdatesStatus() {
        // Arrange
        Long orderId = 1L;
        Order mockOrder = new Order("customer-123");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING);

        // Assert
        assertEquals(OrderStatus.PROCESSING, response.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test
    void updateOrderStatus_InvalidTransition_ThrowsException() {
        // Arrange
        Long orderId = 1L;
        Order mockOrder = new Order("customer-123");
        mockOrder.updateStatus(OrderStatus.PROCESSING);
        mockOrder.updateStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act & Assert
        assertThrows(InvalidOrderStatusTransitionException.class,
            () -> orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING));
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_NonExistingOrder_ThrowsOrderNotFoundException() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class,
            () -> orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING));
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any());
    }
}
