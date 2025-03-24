package com.caom.services;

import com.caom.models.Product;
import com.caom.repos.product.ProductDAO;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * Validates product name - ensures it's not empty and has a reasonable length
     * @param name Product name to validate
     * @return true if name is valid, false otherwise
     */
    public boolean validateProductName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 100;
    }

    /**
     * Validates product price - ensures it's positive
     * @param price Price to validate
     * @return true if price is valid, false otherwise
     */
    public boolean validatePrice(double price) {
        return price >= 0;
    }

    /**
     * Validates product stock - ensures it's not negative
     * @param stock Stock quantity to validate
     * @return true if stock is valid, false otherwise
     */
    public boolean validateStock(int stock) {
        return stock >= 0;
    }

    /**
     * Adds a new product to the database
     * @param name Product name
     * @param description Product description
     * @param price Product price
     * @param stock Initial stock quantity
     * @return The created Product with ID populated, or null if creation failed
     */
    public Product addNewProduct(String name, String description, double price, int stock) {
        if (!validateProductName(name) || !validatePrice(price) || !validateStock(stock)) {
            return null;
        }

        Product productToSave = new Product();
        productToSave.setName(name);
        productToSave.setDescription(description);
        productToSave.setPrice(price);
        productToSave.setStock(stock);

        return productDAO.create(productToSave);
    }

    /**
     * Updates an existing product
     * @param product Product with updated values
     * @return The updated Product if successful, null otherwise
     */
    public Product updateProduct(Product product) {
        if (!validateProductName(product.getName()) ||
                !validatePrice(product.getPrice()) ||
                !validateStock(product.getStock())) {
            return null;
        }

        return productDAO.update(product);
    }

    /**
     * Deletes a product by its ID
     * @param productId ID of product to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProduct(int productId) {
        return productDAO.deleteById(productId);
    }

    /**
     * Retrieves a product by its ID
     * @param productId Product ID to lookup
     * @return The Product if found, null otherwise
     */
    public Product getProductById(int productId) {
        return productDAO.getById(productId);
    }

    /**
     * Retrieves all products
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        return productDAO.getAll();
    }

    /**
     * Retrieves products within a specified price range
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @return List of products in the price range
     */
    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        if (minPrice < 0 || maxPrice < minPrice) {
            return null;
        }

        return productDAO.getProductsByPriceRange(minPrice, maxPrice);
    }

    /**
     * Gets only products that are in stock (stock > 0)
     * @return List of products with available stock
     */
    public List<Product> getAvailableProducts() {
        return productDAO.getProductsInStock();
    }

    /**
     * Updates a product's stock quantity
     * @param productId Product ID
     * @param quantity Quantity to add (positive) or remove (negative)
     * @return true if update was successful, false otherwise
     */
    public boolean updateProductStock(int productId, int quantity) {
        Product product = productDAO.getById(productId);

        // Check if product exists
        if (product == null) {
            return false;
        }

        // Check if we're removing more than available (prevent negative stock)
        if (quantity < 0 && Math.abs(quantity) > product.getStock()) {
            return false;
        }

        return productDAO.updateStock(productId, quantity);
    }

    /**
     * Searches for products by keyword in name or description
     * @param keyword Search term
     * @return List of matching products
     */
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }

        return productDAO.searchProducts(keyword);
    }
}