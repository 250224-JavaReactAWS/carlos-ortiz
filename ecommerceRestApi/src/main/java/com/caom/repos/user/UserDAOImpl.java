package com.caom.repos.user;

import com.caom.models.Role;
import com.caom.models.User;
import com.caom.util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO{
    @Override
    public User getUserByUsername(String username) {

        try(Connection conn = ConnectionUtil.getConnection()){

            String sql = "SELECT * FROM users WHERE email = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                System.out.println(rs.getString("role"));
                u.setRole(Role.valueOf(rs.getString("role")));

                return u;
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve user by username");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public User create(User obj) {
        try (Connection conn = ConnectionUtil.getConnection()){

            String sql = "INSERT INTO users (first_name, last_name, email, password) VALUES " +
                    "(?, ?, ?, ?) RETURNING *;";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, obj.getFirstName());
            ps.setString(2, obj.getLastName());
            ps.setString(3, obj.getEmail());
            ps.setString(4, obj.getPassword());

            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(Role.valueOf(rs.getString("role")));
                return u;
            }

        } catch (SQLException e) {
             System.out.println("Could not save user");
             e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> getAll() {
        List<User> allUsers = new ArrayList<>();
        Connection conn = ConnectionUtil.getConnection();

        String sql = "SELECT * FROM users";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){

                User u = new User();

                u.setUserId(rs.getInt("user_id"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(Role.valueOf(rs.getString("role"))); // We need to cast this to an ENUM

                allUsers.add(u);
            }



        } catch (SQLException e) {
            System.out.println("Could not get all users!");
            e.printStackTrace();
        }


        return allUsers;
    }

    @Override
    public User getById(int id) {
        return null;
    }

    @Override
    public User update(User obj) {
        try(Connection conn = ConnectionUtil.getConnection()) {
        String checkSql = "SELECT * FROM users WHERE user_id = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setInt(1, obj.getUserId());
        ResultSet checkRs = checkPs.executeQuery();

        if(!checkRs.next()) {
            System.out.println("No user found with ID: " + obj.getUserId());
            return null;
        }

        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, " +
                "password = ? WHERE user_id = ? RETURNING *";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, obj.getFirstName());
        ps.setString(2, obj.getLastName());
        ps.setString(3, obj.getEmail());
        ps.setString(4, obj.getPassword());
        ps.setInt(5, obj.getUserId());

        boolean hasResult = ps.execute();

        ResultSet rs;
        if (hasResult) {
            rs = ps.getResultSet();
        } else {
            int count = ps.getUpdateCount();
            System.out.println("Rows updated: " + count);
            if (count > 0) {
                String selectSql = "SELECT * FROM users WHERE user_id = ?";
                PreparedStatement selectPs = conn.prepareStatement(selectSql);
                selectPs.setInt(1, obj.getUserId());
                rs = selectPs.executeQuery();
            } else {
                System.out.println("No rows were updated");
                return null;
            }
        }

        if(rs.next()) {
            User updatedUser = new User();
            updatedUser.setUserId(rs.getInt("user_id"));
            updatedUser.setFirstName(rs.getString("first_name"));
            updatedUser.setLastName(rs.getString("last_name"));
            updatedUser.setEmail(rs.getString("email"));
            updatedUser.setPassword(rs.getString("password"));
            updatedUser.setRole(Role.valueOf(rs.getString("role")));
            return updatedUser;
        } else {
            System.out.println("Failed to retrieve updated user");
        }
        } catch (Exception e){
            System.out.println("Could not update user");
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean deleteById(int id) {
        try(Connection conn = ConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String deleteCartSql = "DELETE FROM cart_item WHERE user_id = ?";
                PreparedStatement psCart = conn.prepareStatement(deleteCartSql);
                psCart.setInt(1, id);
                psCart.executeUpdate();


                String updateOrdersSql = "UPDATE orders SET user_id = null WHERE user_id = ?";
                PreparedStatement psOrders = conn.prepareStatement(updateOrdersSql);
                psOrders.setInt(1, id);
                psOrders.executeUpdate();

                String deleteUserSql = "DELETE FROM users WHERE user_id = ?";
                PreparedStatement psUser = conn.prepareStatement(deleteUserSql);
                psUser.setInt(1, id);
                int rowsAffected = psUser.executeUpdate();

                conn.commit();

                return rowsAffected > 0;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Transaction rolled back. Could not delete user.");
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Could not establish connection for user deletion");
            e.printStackTrace();
            return false;
        }
    }


}
