package com.caom.controllers;

import com.caom.dtos.response.ErrorMessage;
import com.caom.models.Cart;
import com.caom.models.Product;
import com.caom.models.Role;
import com.caom.models.User;
import com.caom.services.CartService;
import com.caom.services.ProductService;
import com.caom.services.UserService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartController {

    private final Logger logger = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    /**
     * Get user's cart items with product details
     */
    public void getUserCartHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to view your cart"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");
        List<Cart> cartItems = cartService.getUserCartItems(userId);
        List<Map<String, Object>> enrichedCartItems = new ArrayList<>();

        // Enrich cart items with product details
        for (Cart item : cartItems) {
            Product product = productService.getProductById(item.getProductId());
            if (product != null) {
                Map<String, Object> enrichedItem = new HashMap<>();
                enrichedItem.put("cartItemId", item.getCartItemId());
                enrichedItem.put("productId", item.getProductId());
                enrichedItem.put("quantity", item.getQuantity());
                enrichedItem.put("productName", product.getName());
                enrichedItem.put("productPrice", product.getPrice());
                enrichedItem.put("totalPrice", product.getPrice() * item.getQuantity());
                enrichedItem.put("inStock", product.getStock() >= item.getQuantity());

                enrichedCartItems.add(enrichedItem);
            }
        }

        // Calculate cart total
        double cartTotal = cartService.calculateCartTotal(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("items", enrichedCartItems);
        response.put("total", cartTotal);

        ctx.status(200);
        ctx.json(response);
    }

    /**
     * Add item to cart
     */
    public void addToCartHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to add items to your cart"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");
        Cart requestCart = ctx.bodyAsClass(Cart.class);

        // Validate request
        if (requestCart.getProductId() <= 0 || requestCart.getQuantity() <= 0) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Invalid product ID or quantity"));
            return;
        }

        // Check if product exists
        Product product = productService.getProductById(requestCart.getProductId());
        if (product == null) {
            ctx.status(404);
            ctx.json(new ErrorMessage("Product not found"));
            return;
        }

        // Add to cart
        Cart addedItem = cartService.addItemToCart(userId, requestCart.getProductId(), requestCart.getQuantity());

        if (addedItem == null) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Could not add item to cart. Insufficient stock available."));
            return;
        }

        logger.info("User ID: " + userId + " added product ID: " + requestCart.getProductId() + " to cart");
        ctx.status(201);
        ctx.json(addedItem);
    }

    /**
     * Update cart item quantity
     */
    public void updateCartItemHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to update your cart"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");
        Cart requestCart = ctx.bodyAsClass(Cart.class);

        // Validate request
        if (requestCart.getCartItemId() <= 0 || requestCart.getQuantity() <= 0) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Invalid cart item ID or quantity"));
            return;
        }

        // Verify ownership of cart item
        if (!cartService.validateCartItemOwnership(userId, requestCart.getCartItemId())) {
            ctx.status(403);
            ctx.json(new ErrorMessage("You can only update items in your own cart"));
            return;
        }

        // Update cart item
        Cart updatedItem = cartService.updateCartItemQuantity(requestCart.getCartItemId(), requestCart.getQuantity());

        if (updatedItem == null) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Could not update cart item. Insufficient stock available."));
            return;
        }

        logger.info("User ID: " + userId + " updated cart item ID: " + requestCart.getCartItemId());
        ctx.status(200);
        ctx.json(updatedItem);
    }

    /**
     * Remove item from cart
     */
    public void removeFromCartHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to remove items from your cart"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");
        int cartItemId = Integer.parseInt(ctx.pathParam("id"));

        // Verify ownership of cart item
        if (!cartService.validateCartItemOwnership(userId, cartItemId)) {
            ctx.status(403);
            ctx.json(new ErrorMessage("You can only remove items from your own cart"));
            return;
        }

        // Remove from cart
        boolean removed = cartService.removeCartItem(cartItemId);

        if (removed) {
            logger.info("User ID: " + userId + " removed cart item ID: " + cartItemId);
            ctx.status(200);
            ctx.json(new ErrorMessage("Item removed from cart successfully"));
        } else {
            ctx.status(400);
            ctx.json(new ErrorMessage("Could not remove item from cart"));
        }
    }

    /**
     * Clear user's cart
     */
    public void clearCartHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to clear your cart"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");

        // Clear cart
        boolean cleared = cartService.clearUserCart(userId);

        if (cleared) {
            logger.info("User ID: " + userId + " cleared their cart");
            ctx.status(200);
            ctx.json(new ErrorMessage("Cart cleared successfully"));
        } else {
            ctx.status(400);
            ctx.json(new ErrorMessage("Could not clear cart"));
        }
    }

    /**
     * Validate cart stock
     */
    public void validateCartStockHandler(Context ctx) {
        // Verify user is logged in
        if(ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to validate your cart"));
            return;
        }

        int userId = ctx.sessionAttribute("userId");

        // Validate cart stock
        boolean valid = cartService.validateCartStock(userId);

        if (valid) {
            ctx.status(200);
            ctx.json(new ErrorMessage("All items in cart have sufficient stock"));
        } else {
            ctx.status(400);
            ctx.json(new ErrorMessage("Some items in your cart have insufficient stock"));
        }
    }
}