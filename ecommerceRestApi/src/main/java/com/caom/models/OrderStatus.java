package com.caom.models;

public enum OrderStatus {
    PENDING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /**
     * Convert a string to an OrderStatus enum value
     * @param status String representation of the status
     * @return The matching OrderStatus or PENDING if not found
     */
    public static OrderStatus fromString(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return PENDING;
        }
    }
}