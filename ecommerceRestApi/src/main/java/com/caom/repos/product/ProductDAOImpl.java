package com.caom.repos.product;

import com.caom.models.Product;
import com.caom.models.Role;
import com.caom.models.User;
import com.caom.util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public Product create(Product obj) {
        try (Connection conn = ConnectionUtil.getConnection()){
            String sql = "INSERT INTO product (name, description, price, stock) VALUES " +
                    "(?, ?, ?, ?) RETURNING *;";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, obj.getName());
            ps.setString(2, obj.getDescription());
            ps.setDouble(3, obj.getPrice());
            ps.setInt(4, obj.getStock());

            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                return product;
            }
        } catch (SQLException e) {
            System.out.println("Could not save product");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> getAll() {
        List<Product> allProducts = new ArrayList<>();

        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM product";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));

                allProducts.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Could not get all products!");
            e.printStackTrace();
        }

        return allProducts;
    }

    @Override
    public Product getById(int id) {
        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM product WHERE product_id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));

                return product;
            }
        } catch (SQLException e) {
            System.out.println("Could not get product by ID: " + id);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Product update(Product obj) {
        try (Connection conn = ConnectionUtil.getConnection()) {
            // First check if the product exists
            String checkSql = "SELECT * FROM product WHERE product_id = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, obj.getProductId());
            ResultSet checkRs = checkPs.executeQuery();

            if (!checkRs.next()) {
                System.out.println("No product found with ID: " + obj.getProductId());
                return null;
            }

            // Product exists, proceed with update
            String sql = "UPDATE product SET name = ?, description = ?, price = ?, " +
                    "stock = ? WHERE product_id = ? RETURNING *";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, obj.getName());
            ps.setString(2, obj.getDescription());
            ps.setDouble(3, obj.getPrice());
            ps.setInt(4, obj.getStock());
            ps.setInt(5, obj.getProductId());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Product updatedProduct = new Product();
                updatedProduct.setProductId(rs.getInt("product_id"));
                updatedProduct.setName(rs.getString("name"));
                updatedProduct.setDescription(rs.getString("description"));
                updatedProduct.setPrice(rs.getDouble("price"));
                updatedProduct.setStock(rs.getInt("stock"));

                return updatedProduct;
            } else {
                System.out.println("Failed to retrieve updated product");
            }
        } catch (SQLException e) {
            System.out.println("Could not update product");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = ConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // First, handle cart items referencing this product
                String deleteCartSql = "DELETE FROM cart_item WHERE product_id = ?";
                PreparedStatement psCart = conn.prepareStatement(deleteCartSql);
                psCart.setInt(1, id);
                psCart.executeUpdate();

                // Handle order items - we might want to keep order history
                // Option 1: Delete order items (not recommended for order history)
                // String deleteOrderItemsSql = "DELETE FROM order_item WHERE product_id = ?";

                // Option 2: Update order items to set product_id to null (if schema allows)
                // String updateOrderItemsSql = "UPDATE order_item SET product_id = null WHERE product_id = ?";

                // Option 3 (implemented): Keep order_item records as is for order history
                // No action needed here as we're preserving the order history

                // Finally, delete the product
                String deleteProductSql = "DELETE FROM product WHERE product_id = ?";
                PreparedStatement psProduct = conn.prepareStatement(deleteProductSql);
                psProduct.setInt(1, id);
                int rowsAffected = psProduct.executeUpdate();

                conn.commit();
                return rowsAffected > 0;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Transaction rolled back. Could not delete product.");
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Could not establish connection for product deletion");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Product getProductByID(int id) {
        return getById(id); // This is the same as getById, so we'll just call that method
    }

    // Additional methods specific to products

    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        List<Product> products = new ArrayList<>();

        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM product WHERE price BETWEEN ? AND ? ORDER BY price ASC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, minPrice);
            ps.setDouble(2, maxPrice);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));

                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Could not get products by price range");
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> getProductsInStock() {
        List<Product> products = new ArrayList<>();

        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM product WHERE stock > 0 ORDER BY stock DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));

                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Could not get products in stock");
            e.printStackTrace();
        }

        return products;
    }

    public boolean updateStock(int productId, int quantity) {
        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "UPDATE product SET stock = stock + ? WHERE product_id = ? AND (stock + ?) >= 0";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, quantity); // Can be positive (add stock) or negative (remove stock)
            ps.setInt(2, productId);
            ps.setInt(3, quantity); // Make sure we don't go below 0

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Could not update product stock");
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();

        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM product WHERE " +
                    "LOWER(name) LIKE ? OR LOWER(description) LIKE ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            String searchParam = "%" + keyword.toLowerCase() + "%";
            ps.setString(1, searchParam);
            ps.setString(2, searchParam);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));

                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Could not search products");
            e.printStackTrace();
        }

        return products;
    }
}
