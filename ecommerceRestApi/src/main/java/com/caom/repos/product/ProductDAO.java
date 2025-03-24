package com.caom.repos.product;

import com.caom.models.Product;
import com.caom.models.User;
import com.caom.repos.GeneralDAO;

import java.util.List;

public interface ProductDAO extends GeneralDAO<Product> {

    /**
     * Create a new product
     * @param obj The Product object to be created
     * @return The created Product with ID populated
     */
    Product create(Product obj);

    /**
     * Get all products
     * @return List of all products
     */
    List<Product> getAll();

    /**
     * Get a product by its ID
     * @param id The ID of the product to retrieve
     * @return The Product if found, null otherwise
     */
    Product getById(int id);

    /**
     * Update an existing product
     * @param obj The Product with updated values
     * @return The updated Product if successful, null otherwise
     */
    Product update(Product obj);

    /**
     * Delete a product by its ID
     * @param id The ID of the product to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteById(int id);

    /**
     * Get a product by its ID (alternative method)
     * @param id The ID of the product to retrieve
     * @return The Product if found, null otherwise
     */
    Product getProductByID(int id);

    /**
     * Get products within a specified price range
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @return List of products within the price range
     */
    List<Product> getProductsByPriceRange(double minPrice, double maxPrice);

    /**
     * Get products that are in stock (quantity > 0)
     * @return List of products with stock available
     */
    List<Product> getProductsInStock();

    /**
     * Update a product's stock quantity
     * @param productId The ID of the product
     * @param quantity The quantity to add (positive) or remove (negative)
     * @return true if update was successful, false otherwise
     */
    boolean updateStock(int productId, int quantity);

    /**
     * Search for products by keyword in name or description
     * @param keyword The search term
     * @return List of products matching the search criteria
     */
    List<Product> searchProducts(String keyword);

}
