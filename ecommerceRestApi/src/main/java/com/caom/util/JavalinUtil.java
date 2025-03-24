package com.caom.util;

import com.caom.controllers.CartController;
import com.caom.controllers.OrderController;
import com.caom.controllers.ProductController;
import com.caom.controllers.UserController;
import com.caom.repos.cart.CartDAO;
import com.caom.repos.cart.CartDAOImpl;
import com.caom.repos.order.OrderDAO;
import com.caom.repos.order.OrderDAOImpl;
import com.caom.repos.product.ProductDAO;
import com.caom.repos.product.ProductDAOImpl;
import com.caom.repos.user.UserDAO;
import com.caom.repos.user.UserDAOImpl;
import com.caom.services.CartService;
import com.caom.services.OrderService;
import com.caom.services.ProductService;
import com.caom.services.UserService;
import io.javalin.Javalin;

import static io.javalin.apibuilder.ApiBuilder.*;

public class JavalinUtil {


    // This class is not explicitly necessary, this can all be done in the main class but since the main class is
    // just for starting the app I'll do my config information here
    // The parent path for all of our resources has been http://localhost:7070
    public static Javalin create(int port){
        // Create all of our variables
        UserDAO userDAO = new UserDAOImpl();
        UserService userService = new UserService(userDAO);
        UserController userController = new UserController(userService);

        ProductDAO productDAO = new ProductDAOImpl();
        ProductService productService = new ProductService(productDAO);
        ProductController productController = new ProductController(productService);

        CartDAO cartDAO = new CartDAOImpl();
        CartService cartService = new CartService(cartDAO, productDAO);
        CartController cartController = new CartController(cartService, productService);

        OrderDAO orderDAO = new OrderDAOImpl();
        OrderService orderService = new OrderService(orderDAO, productDAO);
        OrderController orderController = new OrderController(orderService, userService);

        return Javalin.create(config -> {
                config.router.apiBuilder(() -> {
                    path("/users", () -> {
                        post("/register", userController:: registerUserHandler);
                        put("/update", userController::updateUserHandler);
                        post("/login", userController:: loginHandler);
                        get("/", userController::getAllUsersHandler);
                        delete("/{id}", userController::deleteUserHandler);
                    });
                    path("/products",() -> {
                        post("/", productController::createProductHandler);
                        put("/", productController::updateProductHandler);
                        delete("/{id}", productController::deleteProductHandler);
                        get("/{id}", productController::getProductByIdHandler);
                        get("/", productController::getAllProductsHandler);
                        get("/search", productController::searchProductsHandler);
                        get("/price", productController::getProductsByPriceRangeHandler);
                        get("/in-stock", productController::getProductsInStockHandler);
                        patch("/{id}/stock", productController::updateProductStockHandler);
                    });
                    path("/cart",() -> {
                        get("/", cartController::getUserCartHandler);
                        post("/", cartController::addToCartHandler);
                        put("/", cartController::updateCartItemHandler);
                        delete("/{id}", cartController::removeFromCartHandler);
                        delete("/", cartController::clearCartHandler);
                        get("/validate", cartController::validateCartStockHandler);
                    });
                    path("/orders",() -> {
                        post("/", orderController::createOrderHandler);
                        get("/", orderController::getAllOrdersHandler);
                        get("/me", orderController::getUserOrdersHandler);
                        get("/{id}", orderController::getOrderByIdHandler);
                        put("/{id}/status", orderController::updateOrderStatusHandler);
                        post("/{id}/cancel", orderController::cancelOrderHandler);
                        delete("/{id}", orderController::deleteOrderHandler);
                    });
                });
                })
//                .post("/users/register", ctx -> {userController.registerUserHandler(ctx);})
                // Method Reference Syntax
//                .post("/users/register", userController::registerUserHandler)
//                .post("/users/login", userController::loginHandler)
//                .get("/users", userController::getAllUsersHandler)
                .start(port);
    }
}









