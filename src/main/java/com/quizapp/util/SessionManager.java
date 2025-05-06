package com.quizapp.util;

import com.quizapp.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Manages user sessions and "Remember Me" functionality with enhanced security
 */
public class SessionManager {
    // Use Java Preferences API instead of a properties file for better security
    private static final Preferences PREFS = Preferences.userNodeForPackage(SessionManager.class);
    
    // Keys for storing preferences
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_REMEMBER = "remember";
    
    // Session token management
    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // In-memory session data
    private static String currentUsername = null;
    private static String currentToken = null;
    
    /**
     * Saves user session with enhanced security using token-based authentication
     * 
     * @param username The username to save
     * @param password The password (used to generate a token, not stored)
     * @param remember Whether to remember the user
     * @return A secure token that represents this session
     */
    public static String saveSession(String username, String password, boolean remember) {
        // Clear any existing session data first
        if (!remember) {
            clearSession();
            return null;
        }
        
        try {
            // Generate a secure random token instead of storing the actual password
            String token = generateSecureToken();
            
            // Store username and token
            if (remember) {
                PREFS.put(KEY_USERNAME, username);
                PREFS.put(KEY_TOKEN, token);
                PREFS.putBoolean(KEY_REMEMBER, true);
            }
            
            // Keep in memory
            currentUsername = username;
            currentToken = token;
            
            return token;
        } catch (Exception e) {
            System.err.println("Error saving session: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generates a cryptographically secure random token
     */
    private static String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }
    
    /**
     * Checks if a user session exists and has remember me enabled
     * 
     * @return true if session exists with remember=true
     */
    public static boolean hasRememberedUser() {
        return PREFS.getBoolean(KEY_REMEMBER, false) && 
               PREFS.get(KEY_USERNAME, null) != null && 
               PREFS.get(KEY_TOKEN, null) != null;
    }
    
    /**
     * Gets the remembered username
     * 
     * @return Remembered username or null
     */
    public static String getRememberedUsername() {
        return PREFS.get(KEY_USERNAME, null);
    }
    
    /**
     * Gets the remembered token
     * 
     * @return The session token or null
     */
    public static String getRememberedToken() {
        return PREFS.get(KEY_TOKEN, null);
    }
    
    /**
     * Checks if the given token matches the stored token
     * 
     * @param token The token to validate
     * @return true if the token is valid
     */
    public static boolean validateToken(String token) {
        String storedToken = getRememberedToken();
        return storedToken != null && storedToken.equals(token);
    }
    
    /**
     * Returns the currently stored password (for backward compatibility)
     * This method should be used with caution and will be deprecated
     */
    public static String getRememberedPassword() {
        // This is maintained for backward compatibility but should be avoided
        // in favor of token-based authentication
        System.err.println("Warning: getRememberedPassword is deprecated, use token-based authentication instead");
        return null;
    }
    
    /**
     * Clears the remembered session data
     */
    public static void clearSession() {
        PREFS.remove(KEY_USERNAME);
        PREFS.remove(KEY_TOKEN);
        PREFS.putBoolean(KEY_REMEMBER, false);
        
        // Clear in-memory session data too
        currentUsername = null;
        currentToken = null;
    }
    
    /**
     * Updates the session token (for periodic rotation)
     * 
     * @return The new token
     */
    public static String refreshToken() {
        String username = getRememberedUsername();
        if (username == null) {
            return null;
        }
        
        String newToken = generateSecureToken();
        PREFS.put(KEY_TOKEN, newToken);
        
        // Update in-memory token
        currentToken = newToken;
        
        return newToken;
    }
    
    /**
     * Gets the current in-memory username
     */
    public static String getCurrentUsername() {
        return currentUsername;
    }
    
    /**
     * Gets the current in-memory token
     */
    public static String getCurrentToken() {
        return currentToken;
    }
} 