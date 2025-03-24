package com.caom.controllers;

import com.caom.dtos.response.ErrorMessage;
import com.caom.exceptions.OrderNotFoundException;
import com.caom.exceptions.UnauthorizedActionException;
import com.caom.models.Order;
import com.caom.models.OrderItem;
import com.caom.models.OrderStatus;
import com.caom.models.User;
import com.caom.services.OrderService;
import com.caom.services.UserService;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderController {

    private final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    /**
     * Create a new order
     */
    public void createOrderHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to create an order"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");

        try {
            // Parse the request body
            Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) requestBody.get("items");

            if (itemsData == null || itemsData.isEmpty()) {
                ctx.status(400);
                ctx.json(new ErrorMessage("Order must contain at least one item"));
                return;
            }

            // Convert to OrderItem objects
            List<OrderItem> orderItems = new ArrayList<>();
            for (Map<String, Object> itemData : itemsData) {
                OrderItem item = new OrderItem();
                item.setProductId(((Number) itemData.get("productId")).intValue());
                item.setQuantity(((Number) itemData.get("quantity")).intValue());
                orderItems.add(item);
            }

            // Create the order
            Order createdOrder = orderService.createOrder(userId, orderItems);

            logger.info("User ID: " + userId + " created order ID: " + createdOrder.getOrderId());

            // Return the created order
            ctx.status(201);
            ctx.json(createdOrder);

        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            ctx.status(500);
            ctx.json(new ErrorMessage("An error occurred while creating the order: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * Get all orders (admin only)
     */
    public void getAllOrdersHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to access orders"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");
        User currentUser = userService.getUserById(userId);

        // Check if user is admin
        if (currentUser == null || !currentUser.isAdmin()) {
            ctx.status(403);
            ctx.json(new ErrorMessage("You are not authorized to access all orders"));
            return;
        }

        try {
            List<Order> orders = orderService.getAllOrders();
            ctx.status(200);
            ctx.json(orders);
        } catch (Exception e) {
            logger.error("Error getting all orders: ", e);
            ctx.status(500);
            ctx.json(new ErrorMessage("An error occurred while fetching orders: " + e.getMessage()));
        }
    }

    /**
     * Get orders for the current user
     */
    public void getUserOrdersHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to access your orders"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");

        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);

            // Enrich order data for frontend if needed
            List<Map<String, Object>> enrichedOrders = new ArrayList<>();
            for (Order order : orders) {
                Map<String, Object> enrichedOrder = new HashMap<>();
                enrichedOrder.put("orderId", order.getOrderId());
                enrichedOrder.put("status", order.getStatus());
                enrichedOrder.put("totalPrice", order.getTotalPrice());
                enrichedOrder.put("createdAt", order.getCreatedAt());
                enrichedOrder.put("items", order.getOrderItems());

                enrichedOrders.add(enrichedOrder);
            }

            ctx.status(200);
            ctx.json(enrichedOrders);
        } catch (Exception e) {
            logger.error("Error getting user orders: ", e);
            ctx.status(500);
            ctx.json(new ErrorMessage("An error occurred while fetching your orders: " + e.getMessage()));
        }
    }

    /**
     * Get a specific order by ID
     */
    public void getOrderByIdHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to access orders"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");
        User currentUser = userService.getUserById(userId);

        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            Order order = orderService.getOrderById(orderId);

            // Check if the user is authorized to view this order (admin or owner)
            if (currentUser == null || (!currentUser.isAdmin() && userId != order.getUserId())) {
                ctx.status(403);
                ctx.json(new ErrorMessage("You are not authorized to view this order"));
                return;
            }

            ctx.status(200);
            ctx.json(order);
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Invalid order ID format"));
        } catch (Exception e) {
            logger.error("Error getting order: ", e);
            ctx.status(500);
            ctx.json(new ErrorMessage("An error occurred while fetching the order: " + e.getMessage()));
        }
    }

    /**
     * Update an order's status
     */
    public void updateOrderStatusHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to update orders"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");
        User currentUser = userService.getUserById(userId);

        if (currentUser == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("User not found"));
            return;
        }

        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String statusStr = requestBody.get("status");

            if (statusStr == null || statusStr.trim().isEmpty()) {
                ctx.status(400);
                ctx.json(new ErrorMessage("Status is required"));
                return;
            }

            OrderStatus status = OrderStatus.fromString(statusStr);
            Order updatedOrder = orderService.updateOrderStatus(orderId, status, currentUser);

            logger.info("User ID: " + userId + " updated order ID: " + orderId + " status to " + status);

            ctx.status(200);
            ctx.json(updatedOrder);
        } catch (OrderNotFoundException e) {
            ctx.status(404);
            ctx.json(new ErrorMessage(e.getMessage()));
        } catch (UnauthorizedActionException e) {
            ctx.status(403);
            ctx.json(new ErrorMessage(e.getMessage()));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Invalid order ID format"));
        } catch (Exception e) {
            logger.error("Error updating order status: ", e);
            ctx.status(500);
            ctx.json(new ErrorMessage("An error occurred while updating the order: " + e.getMessage()));
        }
    }

    /**
     * Cancel an order
     */
    public void cancelOrderHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to cancel orders"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");
        User currentUser = userService.getUserById(userId);

        if (currentUser == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("User not found"));
            return;
        }

        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            Order cancelledOrder = orderService.cancelOrder(orderId, currentUser);

            logger.info("User ID: " + userId + " cancelled order ID: " + orderId);

            ctx.status(200);
            ctx.json(cancelledOrder);
        } catch (OrderNotFoundException e) {
            ctx.status(404);
            ctx.json(new ErrorMessage(e.getMessage()));
        } catch (UnauthorizedActionException e) {
            ctx.status(403);
            ctx.json(new ErrorMessage(e.getMessage()));
        } catch (IllegalStateException e) {
            ctx.status(400);
            ctx.json(new ErrorMessage(e.getMessage()));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Invalid order ID format"));
        } catch (Exception e) {
            logger.error("Error cancelling order: ", e);
            ctx.status(500);
            ctx.json(new ErrorMessage("An error occurred while cancelling the order: " + e.getMessage()));
        }
    }

    /**
     * Delete an order (admin only)
     */
    public void deleteOrderHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to delete orders"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");
        User currentUser = userService.getUserById(userId);

        // Check if user is admin
        if (currentUser == null || !currentUser.isAdmin()) {
            ctx.status(403);
            ctx.json(new ErrorMessage("Only administrators can delete orders"));
            return;
        }

        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            boolean deleted = orderService.deleteOrder(orderId, currentUser);

            if (deleted) {
                logger.info("Admin user ID: " + userId + " deleted order ID: " + orderId);
                ctx.status(200);
                ctx.json(new ErrorMessage("Order deleted successfully"));
            } else {
                ctx.status(500);
                ctx.json(new ErrorMessage("Failed to delete the order"));
            }
        } catch (OrderNotFoundException e) {
            ctx.status(404);
            ctx.json(new ErrorMessage(e.getMessage()));
        } catch (UnauthorizedActionException e) {
            ctx.status(403);
            ctx.json(new ErrorMessage(e.getMessage()));
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Invalid order ID format"));
        } catch (Exception e) {
            logger.error("Error deleting order: ", e);
            ctx.status(500);
            ctx.json(new ErrorMessage("An error occurred while deleting the order: " + e.getMessage()));
        }
    }
}