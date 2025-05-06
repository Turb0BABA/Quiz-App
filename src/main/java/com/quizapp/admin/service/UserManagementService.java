package com.quizapp.admin.service;

import com.quizapp.model.User;
import com.quizapp.dao.UserDAO;
import com.quizapp.util.EmailUtil;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for enhanced user management
 */
public class UserManagementService {
    private static final Logger LOGGER = Logger.getLogger(UserManagementService.class.getName());
    private static final long PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 24;
    
    private final UserDAO userDAO;
    
    public UserManagementService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Get all users
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userDAO.findAllUsers();
    }
    
    /**
     * Get user by ID
     * @param userId the user ID
     * @return optional containing the user if found
     */
    public Optional<User> getUserById(int userId) {
        return userDAO.findById(userId);
    }
    
    /**
     * Update a user's information
     * @param user the user to update
     * @return true if update succeeded
     */
    public boolean updateUser(User user) {
        try {
            userDAO.updateUser(user);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating user", e);
            return false;
        }
    }
    
    /**
     * Delete a user
     * @param userId the user ID
     * @return true if deletion succeeded
     */
    public boolean deleteUser(int userId) {
        try {
            userDAO.deleteUser(userId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting user", e);
            return false;
        }
    }
    
    /**
     * Activate or deactivate a user
     * @param userId the user ID
     * @param isActive true to activate, false to deactivate
     * @return true if update succeeded
     */
    public boolean updateUserActiveStatus(int userId, boolean isActive) {
        try {
            userDAO.updateUserStatus(userId, isActive);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating user status", e);
            return false;
        }
    }
    
    /**
     * Generate a password reset token for a user
     * @param userId the user ID
     * @return the generated password reset token, or null if failed
     */
    public String generatePasswordResetToken(int userId) {
        try {
            Optional<User> optUser = userDAO.findById(userId);
            if (optUser.isPresent()) {
                User user = optUser.get();
                
                // Generate a random token
                String token = UUID.randomUUID().toString();
                
                // Set token and expiry time
                user.setPasswordResetToken(token);
                user.setPasswordResetExpiry(Timestamp.valueOf(LocalDateTime.now().plusHours(PASSWORD_RESET_TOKEN_EXPIRY_HOURS)));
                
                userDAO.updatePasswordResetToken(user.getUserId(), token, user.getPasswordResetExpiry());
                return token;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating password reset token", e);
        }
        return null;
    }
    
    /**
     * Reset a user's password using a reset token
     * @param token the password reset token
     * @param newPassword the new password
     * @return true if password reset succeeded
     */
    public boolean resetPasswordWithToken(String token, String newPassword) {
        try {
            Optional<User> optUser = userDAO.findByPasswordResetToken(token);
            if (optUser.isPresent()) {
                User user = optUser.get();
                
                // Check if token is expired
                if (user.getPasswordResetExpiry() != null && 
                    user.getPasswordResetExpiry().before(new Timestamp(System.currentTimeMillis()))) {
                    LOGGER.log(Level.INFO, "Password reset token expired for user ID: " + user.getUserId());
                    return false;
                }
                
                // Reset password
                user.setPassword(newPassword);
                user.setPasswordResetToken(null);
                user.setPasswordResetExpiry(null);
                userDAO.updateUserPassword(user.getUserId(), newPassword);
                userDAO.clearPasswordResetToken(user.getUserId());
                
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error resetting password with token", e);
        }
        return false;
    }
    
    /**
     * Force reset a user's password (administrative function)
     * @param userId the user ID
     * @return the newly generated password, or null if failed
     */
    public String forceResetPassword(int userId) {
        try {
            Optional<User> optUser = userDAO.findById(userId);
            if (optUser.isPresent()) {
                User user = optUser.get();
                
                // Generate a random password
                String newPassword = generateRandomPassword();
                
                // Reset password
                user.setPassword(newPassword);
                userDAO.updateUserPassword(user.getUserId(), newPassword);
                
                return newPassword;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error force resetting password", e);
        }
        return null;
    }
    
    /**
     * Record user's last login time
     * @param userId the user ID
     */
    public void recordUserLogin(int userId) {
        try {
            userDAO.updateLastLoginDate(userId, new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording user login", e);
        }
    }
    
    /**
     * Toggle a user's admin status
     * @param userId the user ID
     * @param isAdmin true to grant admin, false to revoke
     * @return true if update succeeded
     */
    public boolean toggleAdminStatus(int userId, boolean isAdmin) {
        try {
            Optional<User> optUser = userDAO.findById(userId);
            if (optUser.isPresent()) {
                User user = optUser.get();
                user.setAdmin(isAdmin);
                userDAO.updateUserAdminStatus(userId, isAdmin);
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error toggling admin status", e);
        }
        return false;
    }
    
    /**
     * Generate a random password
     * @return random password
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        
        // Generate password with 12 characters
        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
} 