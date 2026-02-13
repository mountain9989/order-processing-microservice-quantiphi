package com.quantiphi.orderservice.service;

import com.quantiphi.orderservice.domain.Order;
import com.quantiphi.orderservice.domain.OrderItem;
import com.quantiphi.orderservice.domain.OrderStatus;
import com.quantiphi.orderservice.dto.CreateOrderRequest;
import com.quantiphi.orderservice.dto.OrderItemRequest;
import com.quantiphi.orderservice.dto.OrderResponse;
import com.quantiphi.orderservice.exception.InvalidOrderStatusTransitionException;
import com.quantiphi.orderservice.exception.OrderNotFoundException;
import com.quantiphi.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing order operations.
 * Handles business logic for order creation, retrieval, and status updates.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Creates a new order with the specified items.
     * Calculates the total price based on item quantities and prices.
     *
     * @param request the order creation request containing customer ID and items
     * @return the created order response with calculated totals and timestamps
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());
        
        Order order = new Order(request.getCustomerId());
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem(
                itemRequest.getProductId(),
                itemRequest.getQuantity(),
                itemRequest.getPrice()
            );
            order.addItem(item);
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("Successfully created order with ID: {} for customer: {}", 
                savedOrder.getId(), savedOrder.getCustomerId());
        
        return OrderResponse.fromEntity(savedOrder);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return the order response with full details
     * @throws OrderNotFoundException if the order does not exist
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        log.debug("Retrieving order with ID: {}", id);
        log.debug("Retrieving order with ID: {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Order not found with ID: {}", id);
                return new OrderNotFoundException(id);
            });
        return OrderResponse.fromEntity(order);
    }

    /**
     * Updates the status of an existing order.
     * Validates the status transition according to business rules.
     *
     * @param id the order ID
     * @param newStatus the new status to transition to
     * @return the updated order response
     * @throws OrderNotFoundException if the order does not exist
     * @throws InvalidOrderStatusTransitionException if the transition is not allowed
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        log.info("Updating order {} to status: {}", id, newStatus);
        log.info("Updating order {} to status: {}", id, newStatus);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Order not found with ID: {}", id);
                return new OrderNotFoundException(id);
            });
        
        try {
            OrderStatus oldStatus = order.getStatus();
            order.updateStatus(newStatus);
            
            Order updatedOrder = orderRepository.save(order);
            log.info("Successfully updated order {} from {} to {}", 
                    id, oldStatus, newStatus);
            
            return OrderResponse.fromEntity(updatedOrder);
        } catch (IllegalStateException e) {
            log.error("Invalid status transition for order {}: {}", id, e.getMessage());
            throw new InvalidOrderStatusTransitionException(e.getMessage());
        }
    }
}
