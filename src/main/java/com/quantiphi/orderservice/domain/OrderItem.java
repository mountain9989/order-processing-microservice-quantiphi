package com.quantiphi.orderservice.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Represents an individual item within an order.
 * Contains product information, quantity, and price calculations.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected OrderItem() {
    }

    /**
     * Creates a new order item with the specified details.
     *
     * @param productId the product identifier
     * @param quantity the quantity ordered
     * @param price the unit price
     */
    public OrderItem(String productId, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * Calculates the subtotal for this item (price × quantity).
     *
     * @return the subtotal amount
     */
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
