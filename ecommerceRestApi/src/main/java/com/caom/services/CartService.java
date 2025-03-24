package com.caom.services;

import com.caom.models.Cart;
import com.caom.models.Product;
import com.caom.repos.cart.CartDAO;
import com.caom.repos.product.ProductDAO;

import java.util.List;

public class CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    /**
     * Get all items in a user's cart
     * @param userId The ID of the user
     * @return List of cart items
     */
    public List<Cart> getUserCartItems(int userId) {
        return cartDAO.getAllByUserId(userId);
    }

    /**
     * Add a product to the user's cart with stock validation
     * @param userId The ID of the user
     * @param productId The ID of the product to add
     * @param quantity The quantity to add
     * @return The Cart object if successful, null otherwise
     */
    public Cart addItemToCart(int userId, int productId, int quantity) {
        // Validate quantity
        if (quantity <= 0) {
            return null;
        }

        // Check if product exists and has enough stock
        Product product = productDAO.getById(productId);
        if (product == null) {
            return null;
        }

        // Check if user already has this product in cart
        Cart existingItem = cartDAO.getByUserAndProductId(userId, productId);
        int totalRequestedQuantity = quantity;

        if (existingItem != null) {
            totalRequestedQuantity += existingItem.getQuantity();
        }

        // Check if there's enough stock
        if (product.getStock() < totalRequestedQuantity) {
            return null; // Not enough stock
        }

        // Add to cart
        return cartDAO.addToCart(userId, productId, quantity);
    }

    /**
     * Update the quantity of a cart item with stock validation
     * @param cartItemId The ID of the cart item
     * @param newQuantity The new quantity
     * @return The updated Cart object if successful, null otherwise
     */
    public Cart updateCartItemQuantity(int cartItemId, int newQuantity) {
        // Validate quantity
        if (newQuantity <= 0) {
            return null;
        }

        // Get the cart item
        Cart cartItem = cartDAO.getById(cartItemId);
        if (cartItem == null) {
            return null;
        }

        // Check if product exists and has enough stock
        Product product = productDAO.getById(cartItem.getProductId());
        if (product == null) {
            return null;
        }

        // Calculate difference in quantity
        int quantityDifference = newQuantity - cartItem.getQuantity();

        // If decreasing quantity, we don't need to check stock
        if (quantityDifference <= 0) {
            cartItem.setQuantity(newQuantity);
            return cartDAO.update(cartItem);
        }

        // Check if there's enough stock for the increase
        if (product.getStock() < quantityDifference) {
            return null; // Not enough stock
        }

        // Update quantity
        cartItem.setQuantity(newQuantity);
        return cartDAO.update(cartItem);
    }

    /**
     * Remove an item from the cart
     * @param cartItemId The ID of the cart item to remove
     * @return true if removal was successful, false otherwise
     */
    public boolean removeCartItem(int cartItemId) {
        return cartDAO.deleteById(cartItemId);
    }

    /**
     * Clear all items from a user's cart
     * @param userId The ID of the user
     * @return true if clearing was successful, false otherwise
     */
    public boolean clearUserCart(int userId) {
        return cartDAO.clearCart(userId);
    }

    /**
     * Validate ownership of a cart item by a user
     * @param userId The ID of the user
     * @param cartItemId The ID of the cart item
     * @return true if the cart item belongs to the user, false otherwise
     */
    public boolean validateCartItemOwnership(int userId, int cartItemId) {
        Cart cartItem = cartDAO.getById(cartItemId);
        return cartItem != null && cartItem.getUserId() == userId;
    }

    /**
     * Calculate the total price of a user's cart
     * @param userId The ID of the user
     * @return The total price
     */
    public double calculateCartTotal(int userId) {
        List<Cart> cartItems = cartDAO.getAllByUserId(userId);
        double total = 0.0;

        for (Cart item : cartItems) {
            Product product = productDAO.getById(item.getProductId());
            if (product != null) {
                total += product.getPrice() * item.getQuantity();
            }
        }

        return total;
    }

    /**
     * Check if all items in a user's cart have sufficient stock
     * @param userId The ID of the user
     * @return true if all items have sufficient stock, false otherwise
     */
    public boolean validateCartStock(int userId) {
        List<Cart> cartItems = cartDAO.getAllByUserId(userId);

        for (Cart item : cartItems) {
            Product product = productDAO.getById(item.getProductId());
            if (product == null || product.getStock() < item.getQuantity()) {
                return false;
            }
        }

        return true;
    }
}