package com.quantiphi.orderservice.controller;

import com.quantiphi.orderservice.dto.CreateOrderRequest;
import com.quantiphi.orderservice.dto.OrderResponse;
import com.quantiphi.orderservice.dto.UpdateOrderStatusRequest;
import com.quantiphi.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for order management operations.
 * Provides endpoints for creating, retrieving, and updating orders.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new order.
     *
     * @param request the order creation request
     * @return the created order with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Received request to create order for customer: {}", request.getCustomerId());
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an order by ID.
     *
     * @param id the order ID
     * @return the order details with HTTP 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        log.info("Received request to retrieve order: {}", id);
        log.info("Received request to retrieve order: {}", id);
        OrderResponse response = orderService.getOrder(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the status of an existing order.
     *
     * @param id the order ID
     * @param request the status update request
     * @return the updated order with HTTP 200 status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("Received request to update order {} status to: {}", id, request.getStatus());
        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}
