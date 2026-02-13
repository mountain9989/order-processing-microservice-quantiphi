package com.quantiphi.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantiphi.orderservice.domain.OrderStatus;
import com.quantiphi.orderservice.dto.CreateOrderRequest;
import com.quantiphi.orderservice.dto.OrderItemRequest;
import com.quantiphi.orderservice.dto.UpdateOrderStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Order Processing API.
 * Tests the full stack from controller to database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeOrderLifecycle_CreateRetrieveAndUpdateStatus_Success() throws Exception {
        // Step 1: Create an order
        CreateOrderRequest createRequest = new CreateOrderRequest(
            "customer-integration-test",
            List.of(
                new OrderItemRequest("PROD-001", 2, BigDecimal.valueOf(15.50)),
                new OrderItemRequest("PROD-002", 1, BigDecimal.valueOf(25.00))
            )
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.customerId").value("customer-integration-test"))
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.totalPrice").value(56.00))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(responseBody).get("id").asLong();

        // Step 2: Retrieve the order
        mockMvc.perform(get("/api/v1/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId))
            .andExpect(jsonPath("$.customerId").value("customer-integration-test"))
            .andExpect(jsonPath("$.status").value("CREATED"));

        // Step 3: Update status to PROCESSING
        UpdateOrderStatusRequest processingUpdate = new UpdateOrderStatusRequest(OrderStatus.PROCESSING);
        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(processingUpdate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PROCESSING"));

        // Step 4: Update status to COMPLETED
        UpdateOrderStatusRequest completedUpdate = new UpdateOrderStatusRequest(OrderStatus.COMPLETED);
        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completedUpdate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Step 5: Verify final state
        mockMvc.perform(get("/api/v1/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void invalidStatusTransition_ReturnsBadRequest() throws Exception {
        // Create an order
        CreateOrderRequest createRequest = new CreateOrderRequest(
            "customer-invalid-transition",
            List.of(new OrderItemRequest("PROD-001", 1, BigDecimal.valueOf(10.00)))
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(responseBody).get("id").asLong();

        // Try to transition directly to COMPLETED (invalid from CREATED)
        UpdateOrderStatusRequest invalidUpdate = new UpdateOrderStatusRequest(OrderStatus.COMPLETED);
        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getOrder_NonExistingId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders/99999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createOrder_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Empty customer ID
        CreateOrderRequest invalidRequest = new CreateOrderRequest(
            "",
            List.of(new OrderItemRequest("PROD-001", 1, BigDecimal.valueOf(10.00)))
        );

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors").exists());
    }
}
