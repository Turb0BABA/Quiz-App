package com.quizapp.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Manages application theme settings including dark/light mode, 
 * animations, font scaling, and high contrast modes.
 */
public class ThemeManager {
    // Theme constants
    public enum Theme {
        LIGHT("Light Theme", new Color(247, 249, 251), new Color(44, 62, 80)),
        DARK("Dark Theme", new Color(33, 33, 33), new Color(236, 240, 241)),
        INTELLIJ("IntelliJ Light", new Color(251, 251, 251), new Color(33, 33, 33)),
        DARCULA("IntelliJ Dark", new Color(43, 43, 43), new Color(187, 187, 187)),
        ARC_LIGHT("Arc Light", new Color(242, 242, 242), new Color(46, 52, 54)),
        ARC_DARK("Arc Dark", new Color(45, 45, 45), new Color(211, 215, 207)),
        NORD("Nord", new Color(46, 52, 64), new Color(216, 222, 233)),
        SOLARIZED_LIGHT("Solarized Light", new Color(253, 246, 227), new Color(101, 123, 131)),
        SOLARIZED_DARK("Solarized Dark", new Color(0, 43, 54), new Color(131, 148, 150));
        
        private final String displayName;
        private final Color backgroundColor;
        private final Color textColor;
        
        Theme(String displayName, Color backgroundColor, Color textColor) {
            this.displayName = displayName;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Color getBackgroundColor() {
            return backgroundColor;
        }
        
        public Color getTextColor() {
            return textColor;
        }
    }
    
    // Properties and default values
    private static Theme currentTheme = Theme.LIGHT;
    private static double fontSizeScale = 1.0;
    private static boolean highContrast = false;
    private static boolean animationsEnabled = false;
    
    // Config file path
    private static final String CONFIG_FILE = "theme_config.properties";
    
    static {
        loadSettings();
    }
    
    /**
     * Loads theme settings from properties file
     */
    public static void loadSettings() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
            
            // Load theme
            String themeName = props.getProperty("theme", Theme.LIGHT.name());
            try {
                currentTheme = Theme.valueOf(themeName);
            } catch (IllegalArgumentException e) {
                currentTheme = Theme.LIGHT;
            }
            
            // Load font scale
            String fontScale = props.getProperty("font.size", "1.0");
            try {
                fontSizeScale = Double.parseDouble(fontScale);
            } catch (NumberFormatException e) {
                fontSizeScale = 1.0;
            }
            
            // Load high contrast
            String highContrastStr = props.getProperty("high.contrast", "false");
            highContrast = Boolean.parseBoolean(highContrastStr);
            
            // Load animations setting
            String animationsStr = props.getProperty("animations.enabled", "false");
            animationsEnabled = Boolean.parseBoolean(animationsStr);
            
        } catch (IOException e) {
            // If file doesn't exist or can't be read, use defaults
            currentTheme = Theme.LIGHT;
            fontSizeScale = 1.0;
            highContrast = false;
            animationsEnabled = false;
        }
    }
    
    /**
     * Saves theme settings to properties file
     */
    public static void saveSettings() {
        Properties props = new Properties();
        props.setProperty("theme", currentTheme.name());
        props.setProperty("font.size", String.valueOf(fontSizeScale));
        props.setProperty("high.contrast", String.valueOf(highContrast));
        props.setProperty("animations.enabled", String.valueOf(animationsEnabled));
        
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Quiz Application Theme Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Applies the current theme to the application
     */
    public static void applyTheme() {
        try {
            switch (currentTheme) {
                case DARK:
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;
                case INTELLIJ:
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    break;
                case DARCULA:
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                    break;
                case ARC_LIGHT:
                    UIManager.setLookAndFeel(new FlatArcIJTheme());
                    break;
                case ARC_DARK:
                    UIManager.setLookAndFeel(new FlatArcDarkIJTheme());
                    break;
                case NORD:
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    customizeNordTheme();
                    break;
                case SOLARIZED_LIGHT:
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    customizeSolarizedLightTheme();
                    break;
                case SOLARIZED_DARK:
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    customizeSolarizedDarkTheme();
                    break;
                case LIGHT:
                default:
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    break;
            }
            
            // Apply font scaling
            applyFontScaling();
            
            // Apply high contrast if enabled
            if (highContrast) {
                applyHighContrast();
            }
            
            // Update all windows
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Applies font scaling to UI components
     */
    private static void applyFontScaling() {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        defaults.keySet().stream()
            .filter(key -> key.toString().endsWith(".font"))
            .forEach(key -> {
                Font font = defaults.getFont(key);
                if (font != null) {
                    float newSize = (float) (font.getSize() * fontSizeScale);
                    defaults.put(key, font.deriveFont(newSize));
                }
            });
    }
    
    /**
     * Applies high contrast settings
     */
    private static void applyHighContrast() {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        
        // Set high contrast colors
        if (isDarkTheme()) {
            // Dark high contrast
            defaults.put("TextField.background", Color.BLACK);
            defaults.put("TextField.foreground", Color.WHITE);
            defaults.put("TextArea.background", Color.BLACK);
            defaults.put("TextArea.foreground", Color.WHITE);
            defaults.put("Button.background", new Color(40, 40, 40));
            defaults.put("Button.foreground", Color.WHITE);
        } else {
            // Light high contrast
            defaults.put("TextField.background", Color.WHITE);
            defaults.put("TextField.foreground", Color.BLACK);
            defaults.put("TextArea.background", Color.WHITE);
            defaults.put("TextArea.foreground", Color.BLACK);
            defaults.put("Button.background", new Color(220, 220, 220));
            defaults.put("Button.foreground", Color.BLACK);
        }
    }
    
    /**
     * Toggles between dark and light themes without animation
     * 
     * @param mainFrame The main application frame
     */
    public static void toggleTheme(JFrame mainFrame) {
        // Toggle between light and dark themes
        if (isDarkTheme()) {
            currentTheme = Theme.LIGHT;
        } else {
            currentTheme = Theme.DARK;
        }
        
        // Simply apply the theme and update UI without animation
        applyTheme();
        SwingUtilities.updateComponentTreeUI(mainFrame);
        saveSettings();
    }
    
    /**
     * Sets the current theme without animation
     * 
     * @param theme The theme to set
     * @param mainFrame The main application frame
     */
    public static void setTheme(Theme theme, JFrame mainFrame) {
        currentTheme = theme;
        
        // Simply apply the theme and update UI without animation
        applyTheme();
        SwingUtilities.updateComponentTreeUI(mainFrame);
        saveSettings();
    }
    
    /**
     * Animates the transition between themes
     * This method is kept for future use but is currently not used
     * 
     * @param frame The frame to animate
     */
    private static void animateThemeTransition(JFrame frame) {
        if (frame == null || !animationsEnabled) {
            // If animations are disabled, just apply the theme directly
            applyTheme();
            SwingUtilities.updateComponentTreeUI(frame);
            return;
        }
        
        // Take a screenshot of the current frame
        JPanel contentPane = (JPanel) frame.getContentPane();
        final BufferedImage screenshot = new BufferedImage(
            contentPane.getWidth(), 
            contentPane.getHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        contentPane.paint(screenshot.getGraphics());
        
        // Create a glass pane for the animation
        JPanel glass = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(screenshot, 0, 0, null);
            }
        };
        glass.setOpaque(false);
        frame.setGlassPane(glass);
        glass.setVisible(true);
        
        // Apply the theme now
        applyTheme();
        SwingUtilities.updateComponentTreeUI(frame);
        
        // Skip animation and make glass pane invisible
        glass.setVisible(false);
    }
    
    /**
     * Sets font size scale factor
     * 
     * @param scale The scale factor (1.0 = 100%)
     * @param mainFrame The main application frame
     */
    public static void setFontSizeScale(double scale, JFrame mainFrame) {
        fontSizeScale = scale;
        applyTheme();
        SwingUtilities.updateComponentTreeUI(mainFrame);
        saveSettings();
    }
    
    /**
     * Sets high contrast mode
     * 
     * @param enabled Whether high contrast is enabled
     * @param mainFrame The main application frame
     */
    public static void setHighContrast(boolean enabled, JFrame mainFrame) {
        highContrast = enabled;
        applyTheme();
        SwingUtilities.updateComponentTreeUI(mainFrame);
        saveSettings();
    }
    
    /**
     * Sets animation enabled state
     * 
     * @param enabled Whether animations are enabled
     */
    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
        saveSettings();
    }
    
    /**
     * Checks if the current theme is a dark theme
     * 
     * @return true if the current theme is dark
     */
    public static boolean isDarkTheme() {
        return currentTheme == Theme.DARK || 
               currentTheme == Theme.DARCULA || 
               currentTheme == Theme.ARC_DARK;
    }
    
    /**
     * Gets the current theme
     * 
     * @return The current theme
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Gets the font size scale factor
     * 
     * @return The font size scale factor
     */
    public static double getFontSizeScale() {
        return fontSizeScale;
    }
    
    /**
     * Checks if high contrast mode is enabled
     * 
     * @return true if high contrast is enabled
     */
    public static boolean isHighContrast() {
        return highContrast;
    }
    
    /**
     * Checks if animations are enabled
     * 
     * @return true if animations are enabled
     */
    public static boolean areAnimationsEnabled() {
        return animationsEnabled;
    }
    
    private static void customizeNordTheme() {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        
        // Nord color palette
        Color nord0 = new Color(46, 52, 64);   // Background
        Color nord1 = new Color(59, 66, 82);   // Selection
        Color nord3 = new Color(67, 76, 94);   // Comment
        Color nord4 = new Color(76, 86, 106);  // Foreground
        Color nord5 = new Color(143, 188, 187);// Cyan
        Color nord6 = new Color(136, 192, 208);// Blue
        Color nord7 = new Color(129, 161, 193);// Light Blue
        Color nord8 = new Color(94, 129, 172); // Dark Blue
        Color nord9 = new Color(191, 97, 106); // Red
        Color nord10 = new Color(208, 135, 112);// Orange
        Color nord11 = new Color(235, 203, 139);// Yellow
        Color nord12 = new Color(163, 190, 140);// Green
        Color nord13 = new Color(180, 142, 173);// Magenta
        Color nord14 = new Color(143, 188, 187);// Cyan
        Color nord15 = new Color(216, 222, 233);// White
        
        defaults.put("Panel.background", nord0);
        defaults.put("Panel.foreground", nord15);
        defaults.put("Button.background", nord1);
        defaults.put("Button.foreground", nord15);
        defaults.put("TextField.background", nord1);
        defaults.put("TextField.foreground", nord15);
        defaults.put("TextArea.background", nord1);
        defaults.put("TextArea.foreground", nord15);
        defaults.put("Menu.background", nord0);
        defaults.put("Menu.foreground", nord15);
        defaults.put("MenuItem.background", nord0);
        defaults.put("MenuItem.foreground", nord15);
    }
    
    private static void customizeSolarizedLightTheme() {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        
        // Solarized Light color palette
        Color base03 = new Color(0, 43, 54);    // Darkest
        Color base02 = new Color(7, 54, 66);    // Dark
        Color base01 = new Color(88, 110, 117); // Dark
        Color base00 = new Color(101, 123, 131);// Dark
        Color base0 = new Color(131, 148, 150); // Light
        Color base1 = new Color(147, 161, 161); // Light
        Color base2 = new Color(238, 232, 213); // Light
        Color base3 = new Color(253, 246, 227); // Lightest
        Color yellow = new Color(181, 137, 0);  // Accent
        Color orange = new Color(203, 75, 22);  // Accent
        Color red = new Color(220, 50, 47);     // Accent
        Color magenta = new Color(211, 54, 130);// Accent
        Color violet = new Color(108, 113, 196);// Accent
        Color blue = new Color(38, 139, 210);   // Accent
        Color cyan = new Color(42, 161, 152);   // Accent
        Color green = new Color(133, 153, 0);   // Accent
        
        defaults.put("Panel.background", base3);
        defaults.put("Panel.foreground", base00);
        defaults.put("Button.background", base2);
        defaults.put("Button.foreground", base00);
        defaults.put("TextField.background", base2);
        defaults.put("TextField.foreground", base00);
        defaults.put("TextArea.background", base2);
        defaults.put("TextArea.foreground", base00);
        defaults.put("Menu.background", base3);
        defaults.put("Menu.foreground", base00);
        defaults.put("MenuItem.background", base3);
        defaults.put("MenuItem.foreground", base00);
    }
    
    private static void customizeSolarizedDarkTheme() {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        
        // Solarized Dark color palette
        Color base03 = new Color(0, 43, 54);    // Darkest
        Color base02 = new Color(7, 54, 66);    // Dark
        Color base01 = new Color(88, 110, 117); // Dark
        Color base00 = new Color(101, 123, 131);// Dark
        Color base0 = new Color(131, 148, 150); // Light
        Color base1 = new Color(147, 161, 161); // Light
        Color base2 = new Color(238, 232, 213); // Light
        Color base3 = new Color(253, 246, 227); // Lightest
        Color yellow = new Color(181, 137, 0);  // Accent
        Color orange = new Color(203, 75, 22);  // Accent
        Color red = new Color(220, 50, 47);     // Accent
        Color magenta = new Color(211, 54, 130);// Accent
        Color violet = new Color(108, 113, 196);// Accent
        Color blue = new Color(38, 139, 210);   // Accent
        Color cyan = new Color(42, 161, 152);   // Accent
        Color green = new Color(133, 153, 0);   // Accent
        
        defaults.put("Panel.background", base03);
        defaults.put("Panel.foreground", base0);
        defaults.put("Button.background", base02);
        defaults.put("Button.foreground", base0);
        defaults.put("TextField.background", base02);
        defaults.put("TextField.foreground", base0);
        defaults.put("TextArea.background", base02);
        defaults.put("TextArea.foreground", base0);
        defaults.put("Menu.background", base03);
        defaults.put("Menu.foreground", base0);
        defaults.put("MenuItem.background", base03);
        defaults.put("MenuItem.foreground", base0);
    }
} 