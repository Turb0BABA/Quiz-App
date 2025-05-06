package com.quizapp.controller;

import com.quizapp.dao.UserDAO;
import com.quizapp.model.User;
import com.quizapp.service.AuthService;

import java.util.Optional;

/**
 * Controller class for authentication-related operations
 */
public class AuthController {
    private final UserDAO userDAO;
    private final AuthService authService;

    public AuthController() {
        this.userDAO = new UserDAO();
        this.authService = AuthService.getInstance();
    }

    /**
     * Authenticate a user with username and password
     * @param username The username
     * @param password The password
     * @return true if authentication succeeds, false otherwise
     */
    public boolean authenticate(String username, String password) {
        return userDAO.authenticate(username, password);
    }

    /**
     * Get a user by username
     * @param username The username
     * @return Optional containing User if found, empty Optional otherwise
     */
    public Optional<User> getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    /**
     * Get a user by ID
     * @param userId The user ID
     * @return Optional containing User if found, empty Optional otherwise
     */
    public Optional<User> getUserById(int userId) {
        return userDAO.findById(userId);
    }

    /**
     * Logout the current user
     */
    public void logout() {
        authService.logout();
    }
} 