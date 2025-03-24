package com.caom.models;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class Order {
    private int orderId;
    private int userId;
    private double totalPrice;
    private OrderStatus status;
    private ZonedDateTime createdAt;
    private List<OrderItem> orderItems;

    // Constructors
    public Order() {
        this.status = OrderStatus.PENDING;
    }

    public Order(int userId, double totalPrice) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING;
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", items=" + (orderItems != null ? orderItems.size() : 0) +
                '}';
    }
}
