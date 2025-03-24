package com.caom.repos.order;

import com.caom.models.Order;
import com.caom.models.OrderItem;
import com.caom.models.OrderStatus;

import java.util.List;

public interface OrderDAO {

    /**
     * Create a new order
     * @param order The order to create
     * @return The created order with generated ID
     */
    Order create(Order order);

    /**
     * Get an order by its ID
     * @param orderId The order ID to find
     * @return The order if found, or null
     */
    Order getById(int orderId);

    /**
     * Get all orders for a specific user
     * @param userId The user ID to find orders for
     * @return List of orders for the user
     */
    List<Order> getOrdersByUserId(int userId);

    /**
     * Get all orders in the system
     * @return List of all orders
     */
    List<Order> getAllOrders();

    /**
     * Update an order's information
     * @param order The order with updated information
     * @return true if successful, false otherwise
     */
    boolean update(Order order);

    /**
     * Update an order's status
     * @param orderId The order ID to update
     * @param status The new status
     * @return true if successful, false otherwise
     */
    boolean updateStatus(int orderId, OrderStatus status);

    /**
     * Delete an order by its ID
     * @param orderId The order ID to delete
     * @return true if successful, false otherwise
     */
    boolean delete(int orderId);

    /**
     * Add an item to an order
     * @param orderItem The order item to add
     * @return The created order item with generated ID
     */
    OrderItem addOrderItem(OrderItem orderItem);

    /**
     * Get all items for a specific order
     * @param orderId The order ID to get items for
     * @return List of order items
     */
    List<OrderItem> getOrderItems(int orderId);

    /**
     * Remove an item from an order
     * @param orderItemId The order item ID to remove
     * @return true if successful, false otherwise
     */
    boolean removeOrderItem(int orderItemId);
}
