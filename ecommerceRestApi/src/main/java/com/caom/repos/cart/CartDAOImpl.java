package com.caom.repos.cart;

import com.caom.models.Cart;
import com.caom.util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAOImpl implements CartDAO {

    @Override
    public List<Cart> getAllByUserId(int userId) {
        List<Cart> userCartItems = new ArrayList<>();

        try(Connection conn = ConnectionUtil.getConnection()){
            String sql = "SELECT * FROM cart_item WHERE user_id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                Cart item = new Cart();

                item.setCartItemId(rs.getInt("cart_item_id"));
                item.setUserId(rs.getInt("user_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));

                userCartItems.add(item);
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve cart items for user ID: " + userId);
            e.printStackTrace();
        }

        return userCartItems;
    }

    @Override
    public Cart create(Cart obj) {
        try (Connection conn = ConnectionUtil.getConnection()){
            String sql = "INSERT INTO cart_item (user_id, product_id, quantity) VALUES (?, ?, ?) RETURNING *";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, obj.getUserId());
            ps.setInt(2, obj.getProductId());
            ps.setInt(3, obj.getQuantity());

            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                Cart newItem = new Cart();
                newItem.setCartItemId(rs.getInt("cart_item_id"));
                newItem.setUserId(rs.getInt("user_id"));
                newItem.setProductId(rs.getInt("product_id"));
                newItem.setQuantity(rs.getInt("quantity"));

                return newItem;
            }
        } catch (SQLException e) {
            System.out.println("Could not add item to cart");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Cart> getAll() {
        List<Cart> allCartItems = new ArrayList<>();

        try(Connection conn = ConnectionUtil.getConnection()){
            String sql = "SELECT * FROM cart_item";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()){
                Cart item = new Cart();
                item.setCartItemId(rs.getInt("cart_item_id"));
                item.setUserId(rs.getInt("user_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));

                allCartItems.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Could not retrieve all cart items");
            e.printStackTrace();
        }

        return allCartItems;
    }

    @Override
    public Cart getById(int id) {
        try (Connection conn = ConnectionUtil.getConnection()){
            String sql = "SELECT * FROM cart_item WHERE cart_item_id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                Cart item = new Cart();
                item.setCartItemId(rs.getInt("cart_item_id"));
                item.setUserId(rs.getInt("user_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));

                return item;
            }
        } catch (SQLException e) {
            System.out.println("Could not retrieve cart item with ID: " + id);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Cart getByUserAndProductId(int userId, int productId) {
        try (Connection conn = ConnectionUtil.getConnection()){
            String sql = "SELECT * FROM cart_item WHERE user_id = ? AND product_id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, productId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                Cart item = new Cart();
                item.setCartItemId(rs.getInt("cart_item_id"));
                item.setUserId(rs.getInt("user_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));

                return item;
            }
        } catch (SQLException e) {
            System.out.println("Could not retrieve cart item for user ID: " + userId + " and product ID: " + productId);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Cart update(Cart obj) {
        try (Connection conn = ConnectionUtil.getConnection()){
            String sql = "UPDATE cart_item SET quantity = ? WHERE cart_item_id = ? RETURNING *";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, obj.getQuantity());
            ps.setInt(2, obj.getCartItemId());

            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                Cart updatedItem = new Cart();
                updatedItem.setCartItemId(rs.getInt("cart_item_id"));
                updatedItem.setUserId(rs.getInt("user_id"));
                updatedItem.setProductId(rs.getInt("product_id"));
                updatedItem.setQuantity(rs.getInt("quantity"));

                return updatedItem;
            }
        } catch (SQLException e) {
            System.out.println("Could not update cart item");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = ConnectionUtil.getConnection()){
            String sql = "DELETE FROM cart_item WHERE cart_item_id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Could not delete cart item with ID: " + id);
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Cart addToCart(int userId, int productId, int quantity) {
        try (Connection conn = ConnectionUtil.getConnection()){
            conn.setAutoCommit(false);

            try {
                // First check if the item already exists in the cart
                String checkSql = "SELECT * FROM cart_item WHERE user_id = ? AND product_id = ?";
                PreparedStatement checkPs = conn.prepareStatement(checkSql);
                checkPs.setInt(1, userId);
                checkPs.setInt(2, productId);

                ResultSet rs = checkPs.executeQuery();

                if (rs.next()) {
                    // Item exists, update quantity
                    int currentQuantity = rs.getInt("quantity");
                    int newQuantity = currentQuantity + quantity;

                    String updateSql = "UPDATE cart_item SET quantity = ? WHERE cart_item_id = ? RETURNING *";
                    PreparedStatement updatePs = conn.prepareStatement(updateSql);
                    updatePs.setInt(1, newQuantity);
                    updatePs.setInt(2, rs.getInt("cart_item_id"));

                    ResultSet updatedRs = updatePs.executeQuery();

                    if (updatedRs.next()) {
                        Cart updatedItem = new Cart();
                        updatedItem.setCartItemId(updatedRs.getInt("cart_item_id"));
                        updatedItem.setUserId(updatedRs.getInt("user_id"));
                        updatedItem.setProductId(updatedRs.getInt("product_id"));
                        updatedItem.setQuantity(updatedRs.getInt("quantity"));

                        conn.commit();
                        return updatedItem;
                    }
                } else {
                    // Item doesn't exist, create new cart item
                    String insertSql = "INSERT INTO cart_item (user_id, product_id, quantity) VALUES (?, ?, ?) RETURNING *";
                    PreparedStatement insertPs = conn.prepareStatement(insertSql);
                    insertPs.setInt(1, userId);
                    insertPs.setInt(2, productId);
                    insertPs.setInt(3, quantity);

                    ResultSet insertedRs = insertPs.executeQuery();

                    if (insertedRs.next()) {
                        Cart newItem = new Cart();
                        newItem.setCartItemId(insertedRs.getInt("cart_item_id"));
                        newItem.setUserId(insertedRs.getInt("user_id"));
                        newItem.setProductId(insertedRs.getInt("product_id"));
                        newItem.setQuantity(insertedRs.getInt("quantity"));

                        conn.commit();
                        return newItem;
                    }
                }

                conn.rollback();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Transaction failed while adding to cart");
                e.printStackTrace();
                return null;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Could not establish connection for cart operation");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean clearCart(int userId) {
        try (Connection conn = ConnectionUtil.getConnection()){
            String sql = "DELETE FROM cart_item WHERE user_id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Could not clear cart for user ID: " + userId);
            e.printStackTrace();
            return false;
        }
    }
}
