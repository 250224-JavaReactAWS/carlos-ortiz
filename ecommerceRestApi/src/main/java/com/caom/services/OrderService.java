package com.caom.services;

import com.caom.exceptions.InsufficientStockException;
import com.caom.exceptions.OrderNotFoundException;
import com.caom.exceptions.ProductNotFoundException;
import com.caom.exceptions.UnauthorizedActionException;
import com.caom.models.*;
import com.caom.repos.order.OrderDAO;
import com.caom.repos.product.ProductDAO;

import java.math.BigDecimal;
import java.util.List;

public class OrderService {

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    public OrderService(OrderDAO orderDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    /**
     * Creates a new order with the given items
     * @param userId User ID of the order owner
     * @param orderItems List of order items to be included in the order
     * @return The created order with ID
     * @throws InsufficientStockException if there is not enough stock for any product
     * @throws InsufficientStockException if any product does not exist
     */
    public Order createOrder(int userId, List<OrderItem> orderItems)
            throws InsufficientStockException, ProductNotFoundException, OrderNotFoundException {
        // Validate stock and calculate total price
        double totalPrice = 0.0;

        // Check stock for each product and hold it temporarily
        for (OrderItem item : orderItems) {
            Product product = productDAO.getById(item.getProductId());
            if (product == null) {
                throw new ProductNotFoundException("Product with ID " + item.getProductId() + " not found");
            }

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStock() +
                        ", Requested: " + item.getQuantity());
            }

            double itemPrice = product.getPrice() * item.getQuantity();
            totalPrice = totalPrice + itemPrice;

            item.setPrice(product.getPrice());

            product.setStock(product.getStock() - item.getQuantity());
            productDAO.update(product);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderDAO.create(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(savedOrder.getOrderId());
            orderDAO.addOrderItem(item);
        }

        return getOrderById(savedOrder.getOrderId());
    }

    /**
     * Gets an order by its ID
     * @param orderId Order ID to retrieve
     * @return The order with the given ID
     * @throws OrderNotFoundException if the order is not found
     */
    public Order getOrderById(int orderId) throws OrderNotFoundException {
        Order order = orderDAO.getById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found");
        }

        // Fetch order items
        List<OrderItem> orderItems = orderDAO.getOrderItems(orderId);
        order.setOrderItems(orderItems);

        return order;
    }

    /**
     * Gets all orders for a specific user
     * @param userId User ID to get orders for
     * @return List of orders for the user
     */
    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = orderDAO.getOrdersByUserId(userId);

        // Fetch order items for each order
        for (Order order : orders) {
            List<OrderItem> orderItems = orderDAO.getOrderItems(order.getOrderId());
            order.setOrderItems(orderItems);
        }

        return orders;
    }

    /**
     * Gets all orders in the system (admin only)
     * @return List of all orders
     */
    public List<Order> getAllOrders() {
        List<Order> orders = orderDAO.getAllOrders();

        // Fetch order items for each order
        for (Order order : orders) {
            List<OrderItem> orderItems = orderDAO.getOrderItems(order.getOrderId());
            order.setOrderItems(orderItems);
        }

        return orders;
    }

    /**
     * Updates the status of an order
     * @param orderId Order ID to update
     * @param status New status for the order
     * @param currentUser User performing the action (for authorization)
     * @return The updated order
     * @throws OrderNotFoundException if the order is not found
     * @throws UnauthorizedActionException if the user is not authorized
     */
    public Order updateOrderStatus(int orderId, OrderStatus status, User currentUser)
            throws OrderNotFoundException, UnauthorizedActionException {

        Order order = getOrderById(orderId);

        // Check authorization (only admin or the owner can update)
        if (!currentUser.isAdmin() && currentUser.getUserId() != order.getUserId()) {
            throw new UnauthorizedActionException("You are not authorized to update this order");
        }

        // Handle stock adjustment if cancelling an order
        if (status == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            // Return items to stock
            restoreStock(order);
        }

        // Update the status
        orderDAO.updateStatus(orderId, status);

        // Return the updated order
        return getOrderById(orderId);
    }

    /**
     * Cancels an order and restores product stock
     * @param orderId Order ID to cancel
     * @param currentUser User performing the action (for authorization)
     * @return The cancelled order
     * @throws OrderNotFoundException if the order is not found
     * @throws UnauthorizedActionException if the user is not authorized
     */
    public Order cancelOrder(int orderId, User currentUser)
            throws OrderNotFoundException, UnauthorizedActionException {

        Order order = getOrderById(orderId);

        // Only allow cancellation of PENDING orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }

        // Check authorization (only admin or the owner can cancel)
        if (!currentUser.isAdmin() && currentUser.getUserId() != order.getUserId()) {
            throw new UnauthorizedActionException("You are not authorized to cancel this order");
        }

        // Update the status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        orderDAO.update(order);

        // Restore stock for all items
        restoreStock(order);

        return order;
    }

    /**
     * Helper method to restore product stock when an order is cancelled
     * @param order The order whose items should be returned to stock
     */
    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = productDAO.getById(item.getProductId());
            if (product != null) {
                // Restore the quantity
                product.setStock(product.getStock() + item.getQuantity());
                productDAO.update(product);
            }
        }
    }

    /**
     * Deletes an order (admin only)
     * @param orderId Order ID to delete
     * @param currentUser User performing the action (for authorization)
     * @return true if successful, false otherwise
     * @throws OrderNotFoundException if the order is not found
     * @throws UnauthorizedActionException if the user is not authorized
     */
    public boolean deleteOrder(int orderId, User currentUser)
            throws OrderNotFoundException, UnauthorizedActionException {

        Order order = getOrderById(orderId);

        // Only admin can delete orders
        if (!currentUser.isAdmin()) {
            throw new UnauthorizedActionException("Only administrators can delete orders");
        }

        // If order is not cancelled, restore stock first
        if (order.getStatus() != OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        // Delete the order
        return orderDAO.delete(orderId);
    }
}