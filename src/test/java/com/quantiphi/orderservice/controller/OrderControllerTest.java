package com.quantiphi.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantiphi.orderservice.domain.OrderStatus;
import com.quantiphi.orderservice.dto.CreateOrderRequest;
import com.quantiphi.orderservice.dto.OrderItemRequest;
import com.quantiphi.orderservice.dto.OrderResponse;
import com.quantiphi.orderservice.dto.UpdateOrderStatusRequest;
import com.quantiphi.orderservice.exception.OrderNotFoundException;
import com.quantiphi.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrder_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
            "customer-123",
            List.of(new OrderItemRequest("A1", 2, BigDecimal.valueOf(10.0)))
        );

        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setCustomerId("customer-123");
        response.setTotalPrice(BigDecimal.valueOf(20.0));
        response.setStatus(OrderStatus.CREATED);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.customerId").value("customer-123"))
            .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createOrder_MissingCustomerId_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
            "",
            List.of(new OrderItemRequest("A1", 2, BigDecimal.valueOf(10.0)))
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_EmptyItems_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("customer-123", List.of());

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_ExistingOrder_ReturnsOk() throws Exception {
        // Arrange
        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setCustomerId("customer-123");
        response.setStatus(OrderStatus.CREATED);

        when(orderService.getOrder(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.customerId").value("customer-123"));
    }

    @Test
    void getOrder_NonExistingOrder_ReturnsNotFound() throws Exception {
        // Arrange
        when(orderService.getOrder(999L))
            .thenThrow(new OrderNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateOrderStatus_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PROCESSING);

        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setStatus(OrderStatus.PROCESSING);

        when(orderService.updateOrderStatus(eq(1L), eq(OrderStatus.PROCESSING)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void updateOrderStatus_MissingStatus_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }
}
