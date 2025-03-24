package com.caom.controllers;

import com.caom.dtos.response.ErrorMessage;
import com.caom.models.Product;
import com.caom.models.Role;
import com.caom.services.ProductService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class ProductController {

    private final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    public void createProductHandler(Context ctx) {
        // Only admin users can create products
        if (ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to create products!"));
            return;
        }

        if (ctx.sessionAttribute("role") != Role.ADMIN) {
            ctx.status(403);
            ctx.json(new ErrorMessage("You must be an admin to create products!"));
            return;
        }

        Product requestProduct = ctx.bodyAsClass(Product.class);

        // Validate product name
        if (!productService.validateProductName(requestProduct.getName())) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Product name is not valid. It must not be empty and must be under 100 characters."));
            return;
        }

        // Validate product price
        if (!productService.validatePrice(requestProduct.getPrice())) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Product price must be greater than or equal to 0."));
            return;
        }

        // Validate product stock
        if (!productService.validateStock(requestProduct.getStock())) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Product stock must be greater than or equal to 0."));
            return;
        }

        Product createdProduct = productService.addNewProduct(
                requestProduct.getName(),
                requestProduct.getDescription(),
                requestProduct.getPrice(),
                requestProduct.getStock());

        if (createdProduct == null) {
            ctx.status(500);
            ctx.json(new ErrorMessage("Something went wrong!"));
            return;
        }

        logger.info("New product created: " + createdProduct.getName());
        ctx.status(201);
        ctx.json(createdProduct);
    }

    public void updateProductHandler(Context ctx) {
        // Only admin users can update products
        if (ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to update products!"));
            return;
        }

        if (ctx.sessionAttribute("role") != Role.ADMIN) {
            ctx.status(403);
            ctx.json(new ErrorMessage("You must be an admin to update products!"));
            return;
        }

        Product requestProduct = ctx.bodyAsClass(Product.class);

        // Validate product name
        if (!productService.validateProductName(requestProduct.getName())) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Product name is not valid. It must not be empty and must be under 100 characters."));
            return;
        }

        // Validate product price
        if (!productService.validatePrice(requestProduct.getPrice())) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Product price must be greater than or equal to 0."));
            return;
        }

        // Validate product stock
        if (!productService.validateStock(requestProduct.getStock())) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Product stock must be greater than or equal to 0."));
            return;
        }

        // Check if product exists
        Product existingProduct = productService.getProductById(requestProduct.getProductId());
        if (existingProduct == null) {
            ctx.status(404);
            ctx.json(new ErrorMessage("Product not found with ID: " + requestProduct.getProductId()));
            return;
        }

        Product updatedProduct = productService.updateProduct(requestProduct);

        if (updatedProduct == null) {
            ctx.status(500);
            ctx.json(new ErrorMessage("Something went wrong!"));
            return;
        }

        logger.info("Updated product with ID: " + updatedProduct.getProductId());
        ctx.status(200);
        ctx.json(updatedProduct);
    }

    public void deleteProductHandler(Context ctx) {
        // Only admin users can delete products
        if (ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to delete products!"));
            return;
        }

        if (ctx.sessionAttribute("role") != Role.ADMIN) {
            ctx.status(403);
            ctx.json(new ErrorMessage("You must be an admin to delete products!"));
            return;
        }

        int productId = Integer.parseInt(ctx.pathParam("id"));

        // Check if product exists
        Product existingProduct = productService.getProductById(productId);
        if (existingProduct == null) {
            ctx.status(404);
            ctx.json(new ErrorMessage("Product not found with ID: " + productId));
            return;
        }

        boolean deleted = productService.deleteProduct(productId);

        if (deleted) {
            logger.info("Product with ID: " + productId + " has been deleted");
            ctx.status(200);
            ctx.json(new ErrorMessage("Product successfully deleted"));
        } else {
            ctx.status(500);
            ctx.json(new ErrorMessage("Failed to delete product"));
        }
    }

    public void getProductByIdHandler(Context ctx) {
        int productId = Integer.parseInt(ctx.pathParam("id"));

        Product product = productService.getProductById(productId);

        if (product == null) {
            ctx.status(404);
            ctx.json(new ErrorMessage("Product not found with ID: " + productId));
            return;
        }

        ctx.status(200);
        ctx.json(product);
    }

    public void getAllProductsHandler(Context ctx) {
        List<Product> products = productService.getAllProducts();
        ctx.status(200);
        ctx.json(products);
    }

    public void searchProductsHandler(Context ctx) {
        String keyword = ctx.queryParam("q");

        if (keyword == null || keyword.trim().isEmpty()) {
            ctx.status(200);
            ctx.json(productService.getAllProducts());
            return;
        }

        List<Product> products = productService.searchProducts(keyword);
        ctx.status(200);
        ctx.json(products);
    }

    public void getProductsByPriceRangeHandler(Context ctx) {
        String minPriceStr = ctx.queryParam("min");
        String maxPriceStr = ctx.queryParam("max");

        try {
            double minPrice = minPriceStr != null ? Double.parseDouble(minPriceStr) : 0;
            double maxPrice = maxPriceStr != null ? Double.parseDouble(maxPriceStr) : Double.MAX_VALUE;

            if (minPrice < 0 || maxPrice < minPrice) {
                ctx.status(400);
                ctx.json(new ErrorMessage("Invalid price range. Min price must be >= 0 and max price must be >= min price."));
                return;
            }

            List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
            ctx.status(200);
            ctx.json(products);
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Invalid price format. Please provide numeric values."));
        }
    }

    public void getProductsInStockHandler(Context ctx) {
        List<Product> products = productService.getAvailableProducts();
        ctx.status(200);
        ctx.json(products);
    }

    public void updateProductStockHandler(Context ctx) {
        // Only admin users can update product stock directly
        if (ctx.sessionAttribute("userId") == null) {
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to update product stock!"));
            return;
        }

        if (ctx.sessionAttribute("role") != Role.ADMIN) {
            ctx.status(403);
            ctx.json(new ErrorMessage("You must be an admin to update product stock!"));
            return;
        }

        int productId = Integer.parseInt(ctx.pathParam("id"));

        try {
            // Assuming we're getting the quantity change from a query parameter or request body
            int quantityChange = Integer.parseInt(ctx.queryParam("quantity"));

            // Check if product exists
            Product existingProduct = productService.getProductById(productId);
            if (existingProduct == null) {
                ctx.status(404);
                ctx.json(new ErrorMessage("Product not found with ID: " + productId));
                return;
            }

            boolean updated = productService.updateProductStock(productId, quantityChange);

            if (updated) {
                logger.info("Updated stock for product ID: " + productId + " by " + quantityChange);
                ctx.status(200);
                ctx.json(new ErrorMessage("Product stock successfully updated"));
            } else {
                ctx.status(400);
                ctx.json(new ErrorMessage("Could not update stock. Make sure there's sufficient stock if removing inventory."));
            }
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.json(new ErrorMessage("Invalid quantity format. Please provide a numeric value."));
        }
    }
}