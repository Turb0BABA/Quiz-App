package com.quizapp;

import com.quizapp.util.DatabaseInitializer;
import com.quizapp.view.auth.LoginFrame;
import com.quizapp.util.SessionManager;
import com.quizapp.dao.UserDAO;
import com.quizapp.model.User;
import com.quizapp.service.AuthService;
import com.quizapp.service.SessionService;
import com.quizapp.util.ThemeManager;
import com.quizapp.util.AccessibilityManager;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Main application class for the Quiz Application
 */
public class App {
    public static void main(String[] args) {
        try {
            // Initialize database
            System.out.println("Initializing database...");
            DatabaseInitializer.initializeDatabase();
            System.out.println("Database initialized successfully");

            // Load application settings
            System.out.println("Loading application settings...");
            ThemeManager.loadSettings();
            AccessibilityManager.loadSettings();
            System.out.println("Application settings loaded successfully");

            // Set look and feel with theme settings
            try {
                System.out.println("Applying theme...");
                ThemeManager.applyTheme();
                System.out.println("Theme applied successfully");
            } catch (Exception e) {
                System.err.println("Failed to apply theme: " + e.getMessage());
                e.printStackTrace();
                
                // Fallback to default look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    System.out.println("Fallback look and feel set successfully");
                } catch (Exception e2) {
                    System.err.println("Failed to set fallback look and feel: " + e2.getMessage());
                    e2.printStackTrace();
                }
            }

            // Adjust font and UI settings for better accessibility
            initializeUISettings();

            // Create and show login frame on EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    // Check for remembered session using the improved SessionService
                    if (SessionService.getInstance().hasValidSession()) {
                        System.out.println("Found remembered session, attempting to restore...");
                        
                        // Try to restore the session using token-based authentication
                        boolean sessionRestored = SessionService.getInstance().restoreSession();
                        
                        if (sessionRestored) {
                            // Get the authenticated user from the service
                            User user = SessionService.getInstance().getCurrentUser();
                            if (user == null) {
                                user = AuthService.getInstance().getCurrentUser();
                            }
                            
                            if (user != null) {
                                System.out.println("Session restored successfully for user: " + user.getUsername());
                                new com.quizapp.view.main.MainFrame(user.getUserId(), user.isAdmin(), user.getUsername()).setVisible(true);
                                return;
                            }
                        }
                        
                        System.out.println("Failed to restore session, showing login screen");
                    }
                    
                    // Fallback: show login frame
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                    System.out.println("Login frame displayed successfully");
                } catch (Exception e) {
                    System.err.println("Failed to create login frame: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
        } catch (Exception e) {
            System.err.println("Application failed to start: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Application failed to start: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        }

    /**
     * Initializes UI settings for better accessibility
     */
    private static void initializeUISettings() {
        // Set global UI settings for better accessibility
        UIManager.put("ToolTip.font", new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        UIManager.put("ToolTip.background", new Color(255, 255, 225));
        UIManager.put("ToolTip.foreground", Color.BLACK);
        
        // Increase focus visibility for better keyboard navigation
        UIManager.put("Button.focusWidth", 2);
        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Component.innerFocusWidth", 1);
        UIManager.put("Component.focusColor", new Color(66, 153, 225));
        
        // Explicitly disable animations for better performance
        ThemeManager.setAnimationsEnabled(false);
        
        // Apply high contrast if enabled
        if (ThemeManager.isHighContrast()) {
            if (ThemeManager.isDarkTheme()) {
                // High contrast for dark theme
                UIManager.put("TextField.background", Color.BLACK);
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("TextArea.background", Color.BLACK);
                UIManager.put("TextArea.foreground", Color.WHITE);
            } else {
                // High contrast for light theme
                UIManager.put("TextField.background", Color.WHITE);
                UIManager.put("TextField.foreground", Color.BLACK);
                UIManager.put("TextArea.background", Color.WHITE);
                UIManager.put("TextArea.foreground", Color.BLACK);
            }
        }
        
        // Apply font scaling
        double fontScale = ThemeManager.getFontSizeScale();
        if (fontScale != 1.0) {
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            defaults.keySet().stream()
                .filter(key -> key.toString().endsWith(".font"))
                .forEach(key -> {
                    Font font = defaults.getFont(key);
                    if (font != null) {
                        float newSize = (float) (font.getSize() * fontScale);
                        defaults.put(key, font.deriveFont(newSize));
                    }
                });
        }
    }
}