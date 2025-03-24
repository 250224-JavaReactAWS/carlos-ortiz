package com.caom.services;

import com.caom.models.User;
import com.caom.repos.user.UserDAO;

import java.util.List;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO){
        this.userDAO = userDAO;
    }

    public boolean validateUsername(String username){
        return username.length() >= 8;
    }
    public boolean isUsernameAvailable(String username){
        return userDAO.getUserByUsername(username) == null;
    }

    public boolean validatePassword(String password){
        boolean correctLength = password.length() >= 8;
        boolean hasLowercase = false;
        boolean hasUppercase = false;
        char[] characters = password.toCharArray();
        for (char c: characters){
            if (Character.isUpperCase(c)){
                hasUppercase = true;
            } else if (Character.isLowerCase(c)){
                hasLowercase = true;
            }
        }

        return correctLength && hasLowercase && hasUppercase;
    }

    public User registerNewUser(String firstName, String lastName, String username, String password){
        User userToBeSaved = new User(firstName, lastName, username, password);
        return userDAO.create(userToBeSaved);

    }

    public User updateUser(User obj){
        return userDAO.update(obj);
    }

    public boolean deleteUser(int userId) {
        return userDAO.deleteById(userId);
    }

    public User loginUser(String username, String password){
        User returnedUser = userDAO.getUserByUsername(username);
        if (returnedUser == null){
            return null;
        }
        if (returnedUser.getPassword().equals(password)){
            return returnedUser;
        }
        return null;
    }

    public List<User> getAllUsers(){
        return userDAO.getAll();
    }

    public User getUserById(int userId) {
        return userDAO.getById(userId);
    }

    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        return user.getRole() != null && user.getRole().equals("ADMIN");
    }

}
