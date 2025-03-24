package com.caom.controllers;

import com.caom.dtos.response.ErrorMessage;
import com.caom.models.Role;
import com.caom.models.User;
import com.caom.services.UserService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void registerUserHandler(Context ctx){
        User requestUser = ctx.bodyAsClass(User.class);
        if (!userService.validateUsername(requestUser.getEmail())){
            ctx.status(400);
            ctx.json(new ErrorMessage("Username is not valid. It must be at least 8 characters"));
            return;
        }

        if (!userService.validatePassword(requestUser.getPassword())){
            ctx.status(400);
            ctx.json(new ErrorMessage("Password is not valid. It must be at least 8 characters and contain a capital " +
                    "and lowercase letter"));
            return;
        }
        if (!userService.isUsernameAvailable(requestUser.getEmail())){
            ctx.status(400);
            ctx.json(new ErrorMessage("Username is not available, please select a new one"));
            logger.warn("Register attempt made for taken username: " + requestUser.getEmail());
            return;
        }

        User registeredUser = userService.registerNewUser(
                requestUser.getFirstName(),
                requestUser.getLastName(),
                requestUser.getEmail(),
                requestUser.getPassword());
        if (registeredUser == null){
            ctx.status(500);
            ctx.json(new ErrorMessage("Something went wrong!"));
            return;
        }
        logger.info("New user registered with username: " + registeredUser.getEmail());
        ctx.status(201);
        ctx.json(registeredUser);

    }


    public void updateUserHandler(Context ctx){
        User requestUser = ctx.bodyAsClass(User.class);

        if (!userService.validateUsername(requestUser.getEmail())){
            ctx.status(400);
            ctx.json(new ErrorMessage("Username is not valid. It must be at least 8 characters"));
            return;
        }

        if (!userService.validatePassword(requestUser.getPassword())){
            ctx.status(400);
            ctx.json(new ErrorMessage("Password is not valid. It must be at least 8 characters and contain a capital " +
                    "and lowercase letter"));
            return;
        }

        if(ctx.sessionAttribute("userId") == null){
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to view this method!"));
            return;
        }else{
            System.out.println(ctx.sessionAttribute("userId").toString());
            System.out.println(String.valueOf(requestUser.getUserId()));
            if(!ctx.sessionAttribute("userId").toString().equals(String.valueOf(requestUser.getUserId()))){
                ctx.status(401);
                ctx.json(new ErrorMessage("Must be the same user to modify the information."));
                return;
            }
        }

        User user = new User(
                requestUser.getUserId(),
                requestUser.getFirstName(),
                requestUser.getLastName(),
                requestUser.getEmail(),
                requestUser.getPassword());

        User updatedUser = userService.updateUser(user);
        if (updatedUser == null){
            ctx.status(500);
            ctx.json(new ErrorMessage("Something went wrong!"));
            return;
        }
        logger.info("Updated user with email: " + updatedUser.getEmail());
        ctx.status(201);
        ctx.json(updatedUser);

    }

    public void loginHandler(Context ctx){
        User requestUser = ctx.bodyAsClass(User.class);
        User returnedUser = userService.loginUser(requestUser.getEmail(), requestUser.getPassword());
        if (returnedUser == null){
            ctx.json(new ErrorMessage("Username or Password Incorrect"));
            ctx.status(400);
            return;
        }
        ctx.status(200);
        ctx.json(returnedUser);

        ctx.sessionAttribute("userId", returnedUser.getUserId());
        ctx.sessionAttribute("role", returnedUser.getRole());
    }

    public void getAllUsersHandler(Context ctx){
        if(ctx.sessionAttribute("userId") == null){
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to view this method!"));
            return;
        }
        if (ctx.sessionAttribute("role") != Role.ADMIN){
            ctx.status(403);
            ctx.json(new ErrorMessage("You must be an admin to access this endpoint!"));
            return;
        }
        ctx.json(userService.getAllUsers());
    }

    public void deleteUserHandler(Context ctx) {
        if(ctx.sessionAttribute("userId") == null){
            ctx.status(401);
            ctx.json(new ErrorMessage("You must be logged in to use this method!"));
            return;
        }

        int userId = Integer.parseInt(ctx.pathParam("id"));

        if(!ctx.sessionAttribute("userId").toString().equals(String.valueOf(userId)) &&
                ctx.sessionAttribute("role") != Role.ADMIN) {
            ctx.status(403);
            ctx.json(new ErrorMessage("You can only delete your own account or must be an admin!"));
            return;
        }

        boolean deleted = userService.deleteUser(userId);

        if(deleted) {
            if(ctx.sessionAttribute("userId").toString().equals(String.valueOf(userId))) {
                ctx.req().getSession().invalidate();
            }

            logger.info("User with ID: " + userId + " has been deleted");
            ctx.status(200);
            ctx.json(new ErrorMessage("User successfully deleted"));
        } else {
            ctx.status(500);
            ctx.json(new ErrorMessage("Failed to delete user"));
        }
    }
}
