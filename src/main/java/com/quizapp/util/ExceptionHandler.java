package com.quizapp.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Global exception handler for the application.
 * Provides methods to handle different types of exceptions consistently.
 */
public class ExceptionHandler {
    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());
    
    // Prevent instantiation
    private ExceptionHandler() {
    }
    
    /**
     * Handle a general exception
     * @param e the exception
     * @param message a message to display to the user
     */
    public static void handle(Exception e, String message) {
        LOGGER.log(Level.SEVERE, message, e);
        
        // Display a user-friendly error message
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(
                null,
                message + "\n\nError: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        );
    }
    
    /**
     * Handle a general exception with default message
     * @param e the exception
     */
    public static void handle(Exception e) {
        handle(e, "An unexpected error occurred.");
    }
    
    /**
     * Handle a database exception
     * @param e the SQL exception
     * @param message a message to display to the user
     */
    public static void handleDatabaseError(SQLException e, String message) {
        LOGGER.log(Level.SEVERE, message, e);
        
        // Get more detailed SQL error information
        String sqlState = e.getSQLState();
        int errorCode = e.getErrorCode();
        String detailedMessage = "SQL State: " + sqlState + ", Error Code: " + errorCode;
        
        LOGGER.log(Level.SEVERE, detailedMessage);
        
        // Display a user-friendly error message
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(
                null,
                message + "\n\nDatabase error. Please try again later or contact support.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            )
        );
    }
    
    /**
     * Handle a database exception with default message
     * @param e the SQL exception
     */
    public static void handleDatabaseError(SQLException e) {
        handleDatabaseError(e, "A database error occurred.");
    }
    
    /**
     * Log a serious error with full stack trace, but don't show a dialog
     * @param e the exception
     * @param message the message to log
     */
    public static void logSevere(Throwable e, String message) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        
        LOGGER.log(Level.SEVERE, message + "\n" + stackTrace);
    }
} 