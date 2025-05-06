package com.quizapp.service;

import com.quizapp.dao.UserDAO;
import com.quizapp.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Service for handling user authentication with improved token-based support
 */
public class AuthService {
    private static AuthService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Login a user with username and password
     * 
     * @param username The username
     * @param password The password
     * @return True if authentication succeeds
     */
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        boolean isAuthenticated = userDAO.authenticate(username, password);
        if (isAuthenticated) {
            Optional<User> user = userDAO.findByUsername(username);
            user.ifPresent(u -> currentUser = u);
            
            // Update last login time
            if (currentUser != null) {
                userDAO.updateLastLogin(currentUser.getUserId());
            }
        }
        return isAuthenticated;
    }

    /**
     * Set current user directly (used for token-based authentication)
     * 
     * @param user The user to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Logout the current user
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Get the currently logged-in user
     * 
     * @return The current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     * 
     * @return True if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if the current user is an admin
     * 
     * @return True if the current user is an admin
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Register a new user
     * 
     * @param username The username
     * @param password The password
     * @param email The email
     * @param isAdmin Whether the user is an admin
     * @return True if registration succeeds
     */
    public boolean registerUser(String username, String password, String email, boolean isAdmin) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty() || 
            email == null || email.trim().isEmpty()) {
            return false;
        }

        // Check if user already exists
        if (userDAO.findByUsername(username).isPresent()) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(password); // UserDAO will hash this
        newUser.setEmail(email);
        newUser.setAdmin(isAdmin);

        try {
            userDAO.create(newUser);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 