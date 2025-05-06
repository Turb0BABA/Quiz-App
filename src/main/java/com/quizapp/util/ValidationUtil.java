package com.quizapp.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class for input validation
 */
public class ValidationUtil {
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    // Username validation pattern (alphanumeric, 3-20 chars)
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    
    // Password validation (at least 6 chars)
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^.{6,}$");
    
    /**
     * Validate email format
     * @param email the email to validate
     * @return true if email is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
    
    /**
     * Validate username format
     * @param username the username to validate
     * @return true if username is valid
     */
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        Matcher matcher = USERNAME_PATTERN.matcher(username);
        return matcher.matches();
    }
    
    /**
     * Validate password format
     * @param password the password to validate
     * @return true if password is valid
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }
    
    /**
     * Check if a string is empty or null
     * @param str the string to check
     * @return true if string is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Validate integer input
     * @param input the input string
     * @return true if input is a valid integer
     */
    public static boolean isValidInteger(String input) {
        if (isEmpty(input)) {
            return false;
        }
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate integer input within range
     * @param input the input string
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return true if input is a valid integer within range
     */
    public static boolean isValidIntegerInRange(String input, int min, int max) {
        if (!isValidInteger(input)) {
            return false;
        }
        int value = Integer.parseInt(input);
        return value >= min && value <= max;
    }
} 