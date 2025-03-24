package com.caom.repos.cart;

import com.caom.models.Cart;

import java.util.List;

public interface CartDAO {

    /**
     * Get all cart items for a specific user
     * @param userId The ID of the user whose cart items to retrieve
     * @return List of Cart objects belonging to the user
     */
    List<Cart> getAllByUserId(int userId);

    /**
     * Create a new cart item
     * @param obj The Cart object to be created
     * @return The created Cart object with ID populated
     */
    Cart create(Cart obj);

    /**
     * Get all cart items in the system
     * @return List of all Cart objects
     */
    List<Cart> getAll();

    /**
     * Get a cart item by its ID
     * @param id The ID of the cart item to retrieve
     * @return The Cart object if found, null otherwise
     */
    Cart getById(int id);

    /**
     * Update an existing cart item
     * @param obj The Cart object with updated values
     * @return The updated Cart object if successful, null otherwise
     */
    Cart update(Cart obj);

    /**
     * Delete a cart item by its ID
     * @param id The ID of the cart item to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteById(int id);

    /**
     * Add or update an item in a user's cart
     * @param userId The ID of the user
     * @param productId The ID of the product to add
     * @param quantity The quantity to add or update
     * @return The updated or created Cart object
     */
    Cart addToCart(int userId, int productId, int quantity);

    /**
     * Remove all items from a user's cart
     * @param userId The ID of the user whose cart to clear
     * @return true if operation was successful, false otherwise
     */
    boolean clearCart(int userId);

    /**
     * Get cart item by user ID and product ID
     * @param userId The ID of the user
     * @param productId The ID of the product
     * @return The Cart object if found, null otherwise
     */
    Cart getByUserAndProductId(int userId, int productId);
}