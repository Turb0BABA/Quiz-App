package com.quizapp.util;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages accessibility features for the application
 * to support screen readers and other assistive technologies.
 */
public class AccessibilityManager {
    
    // Properties and default values
    private static boolean screenReaderOptimized = false;
    private static boolean highContrast = false;
    private static double textScale = 1.0;
    private static boolean highFocusMode = false;
    private static int keyboardNavigationLevel = 1;
    
    // Config file path
    private static final String CONFIG_FILE = "accessibility_config.properties";
    
    static {
        loadSettings();
    }
    
    /**
     * Checks if high focus mode is enabled
     * 
     * @return true if high focus mode is enabled
     */
    public static boolean isHighFocusMode() {
        return highFocusMode;
    }
    
    /**
     * Gets the keyboard navigation level
     * 
     * @return keyboard navigation level (1=basic, 2=advanced)
     */
    public static int getKeyboardNavigationLevel() {
        return keyboardNavigationLevel;
    }
    
    /**
     * Loads accessibility settings from properties file
     */
    public static void loadSettings() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
            
            // Load screen reader optimization setting
            String screenReaderStr = props.getProperty("screen.reader.optimized", "false");
            screenReaderOptimized = Boolean.parseBoolean(screenReaderStr);
            
            // Load high contrast setting
            String highContrastStr = props.getProperty("high.contrast", "false");
            highContrast = Boolean.parseBoolean(highContrastStr);
            
            // Load text scale
            String textScaleStr = props.getProperty("text.scale", "1.0");
            try {
                textScale = Double.parseDouble(textScaleStr);
            } catch (NumberFormatException e) {
                textScale = 1.0;
            }
            
        } catch (IOException e) {
            // If file doesn't exist or can't be read, use defaults
            screenReaderOptimized = false;
            highContrast = false;
            textScale = 1.0;
        }
    }
    
    /**
     * Saves accessibility settings to properties file
     */
    public static void saveSettings() {
        Properties props = new Properties();
        props.setProperty("screen.reader.optimized", String.valueOf(screenReaderOptimized));
        props.setProperty("high.contrast", String.valueOf(highContrast));
        props.setProperty("text.scale", String.valueOf(textScale));
        
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Quiz Application Accessibility Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets screen reader optimization
     * 
     * @param enabled Whether screen reader optimization is enabled
     * @param mainFrame The main application frame to update
     */
    public static void setScreenReaderOptimized(boolean enabled, JFrame mainFrame) {
        screenReaderOptimized = enabled;
        applyAccessibilitySettings(mainFrame);
        saveSettings();
    }
    
    /**
     * Sets high contrast mode
     * 
     * @param enabled Whether high contrast is enabled
     * @param mainFrame The main application frame to update
     */
    public static void setHighContrast(boolean enabled, JFrame mainFrame) {
        highContrast = enabled;
        ThemeManager.setHighContrast(enabled, mainFrame);
        saveSettings();
    }
    
    /**
     * Sets text scale factor
     * 
     * @param scale The text scale factor (1.0 = 100%)
     * @param mainFrame The main application frame to update
     */
    public static void setTextScale(double scale, JFrame mainFrame) {
        textScale = scale;
        ThemeManager.setFontSizeScale(scale, mainFrame);
        saveSettings();
    }
    
    /**
     * Checks if screen reader optimization is enabled
     * 
     * @return true if screen reader optimization is enabled
     */
    public static boolean isScreenReaderOptimized() {
        return screenReaderOptimized;
    }
    
    /**
     * Applies accessibility settings to the application
     * 
     * @param mainFrame The main application frame to update
     */
    public static void applyAccessibilitySettings(JFrame mainFrame) {
        if (mainFrame == null) return;
        
        if (screenReaderOptimized) {
            optimizeForScreenReaders(mainFrame);
        }
    }
    
    /**
     * Optimizes the application for screen readers
     * 
     * @param component The component to optimize (typically the main frame)
     */
    private static void optimizeForScreenReaders(Component component) {
        if (component == null) return;
        
        // For containers, iterate through their children
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                optimizeForScreenReaders(child);
            }
        }
        
        // Apply specific optimizations based on component type
        if (component instanceof JButton) {
            JButton button = (JButton) component;
            // Ensure buttons have accessible descriptions
            if (button.getAccessibleContext().getAccessibleDescription() == null) {
                button.getAccessibleContext().setAccessibleDescription(button.getText() + " button");
            }
        } else if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            // Ensure labels have accessible names
            if (label.getAccessibleContext().getAccessibleName() == null) {
                label.getAccessibleContext().setAccessibleName(label.getText());
            }
        } else if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            // Set role descriptions for panels
            AccessibleContext context = panel.getAccessibleContext();
            if (context != null && context.getAccessibleDescription() == null) {
                String name = panel.getName();
                if (name != null && !name.isEmpty()) {
                    context.setAccessibleDescription(name + " panel");
                }
            }
        }
    }
    
    /**
     * Sets accessible description for a component
     * 
     * @param component The component
     * @param description The accessible description
     */
    public static void setAccessibleDescription(JComponent component, String description) {
        if (component != null) {
            component.getAccessibleContext().setAccessibleDescription(description);
        }
    }
    
    /**
     * Sets accessible name for a component
     * 
     * @param component The component
     * @param name The accessible name
     */
    public static void setAccessibleName(JComponent component, String name) {
        if (component != null) {
            component.getAccessibleContext().setAccessibleName(name);
        }
    }
    
    /**
     * Makes components in a form accessible by setting proper focus order
     * 
     * @param components The components in the desired focus order
     */
    public static void setFocusOrder(JComponent... components) {
        for (int i = 0; i < components.length - 1; i++) {
            final int nextIndex = i + 1;
            components[i].setNextFocusableComponent(components[nextIndex]);
        }
        
        // Make the focus order circular if there are components
        if (components.length > 0) {
            components[components.length - 1].setNextFocusableComponent(components[0]);
        }
    }
    
    /**
     * Applies focus highlighting to the specified component
     * Adds a visible border around focused elements
     * 
     * @param component The component to apply focus highlighting to
     */
    public static void applyFocusHighlighting(JComponent component) {
        if (isHighFocusMode()) {
            // Store original border
            Border originalBorder = component.getBorder();
            
            // Add focus listener
            component.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    // Add highlighted border when focused
                    component.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), 2),
                        originalBorder
                    ));
                }
                
                @Override
                public void focusLost(FocusEvent e) {
                    // Restore original border when focus is lost
                    component.setBorder(originalBorder);
                }
            });
        }
    }
    
    /**
     * Enables focus mode for a panel containing questions
     * This highlights the current question and options to improve focus
     * 
     * @param questionPanel The panel containing the question
     * @param optionsPanel The panel containing the answer options
     */
    public static void enableFocusMode(JPanel questionPanel, JPanel optionsPanel) {
        if (isHighFocusMode()) {
            // Add a subtle background to the question panel
            questionPanel.setOpaque(true);
            questionPanel.setBackground(new Color(245, 245, 250));
            questionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 240), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            
            // Make options more distinct
            for (Component comp : optionsPanel.getComponents()) {
                if (comp instanceof JRadioButton) {
                    JRadioButton option = (JRadioButton) comp;
                    option.setOpaque(true);
                    option.setBackground(new Color(252, 252, 255));
                    option.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 240), 1),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                    ));
                    
                    // Add hover effect
                    option.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            option.setBackground(new Color(240, 240, 250));
                        }
                        
                        @Override
                        public void mouseExited(MouseEvent e) {
                            option.setBackground(new Color(252, 252, 255));
                        }
                    });
                }
            }
        }
    }
    
    /**
     * Enhances keyboard navigation for a component
     * Especially useful for tables and complex components
     * 
     * @param component The component to enhance
     */
    public static void enhanceKeyboardNavigation(JComponent component) {
        // Add key bindings for enhanced navigation
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        
        // Home key navigates to first element
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "navigateFirst");
        actionMap.put("navigateFirst", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (component instanceof JTable) {
                    JTable table = (JTable) component;
                    if (table.getRowCount() > 0) {
                        table.setRowSelectionInterval(0, 0);
                        table.scrollRectToVisible(table.getCellRect(0, 0, true));
                    }
                }
            }
        });
        
        // End key navigates to last element
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "navigateLast");
        actionMap.put("navigateLast", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (component instanceof JTable) {
                    JTable table = (JTable) component;
                    int lastRow = table.getRowCount() - 1;
                    if (lastRow >= 0) {
                        table.setRowSelectionInterval(lastRow, lastRow);
                        table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
                    }
                }
            }
        });
    }
    
    /**
     * Registers additional keyboard shortcuts beyond the basics
     * 
     * @param contentPane The content pane to register shortcuts with
     */
    public static void registerAdvancedKeyboardShortcuts(JComponent contentPane) {
        if (getKeyboardNavigationLevel() > 1) { // Advanced level
            // Filter/search shortcut (Ctrl+F)
            /* Commenting out since KeyboardShortcutManager might not be properly implemented
            KeyboardShortcutManager.registerShortcut(
                contentPane,
                "search",
                () -> {
                    // Find any visible search field and focus it
                    Container root = contentPane.getTopLevelAncestor();
                    if (root instanceof JFrame) {
                        JFrame frame = (JFrame) root;
                        findAndFocusSearchField(frame.getContentPane());
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK)
            );
            
            // Escape for cancel/back
            KeyboardShortcutManager.registerShortcut(
                contentPane,
                "escape",
                () -> {
                    // Find and click a visible back or close button
                    Container root = contentPane.getTopLevelAncestor();
                    if (root instanceof JFrame) {
                        JFrame frame = (JFrame) root;
                        findAndClickBackButton(frame.getContentPane());
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)
            );
            
            // Save shortcut (Ctrl+S)
            KeyboardShortcutManager.registerShortcut(
                contentPane,
                "save",
                () -> {
                    // Find and click a visible save button
                    Container root = contentPane.getTopLevelAncestor();
                    if (root instanceof JFrame) {
                        JFrame frame = (JFrame) root;
                        findAndClickSaveButton(frame.getContentPane());
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
            );
            */
        }
    }
    
    /**
     * Helper method to find and focus a search field
     */
    private static boolean findAndFocusSearchField(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                String name = textField.getName();
                if (name != null && name.toLowerCase().contains("search") || 
                    textField.getToolTipText() != null && 
                    textField.getToolTipText().toLowerCase().contains("search")) {
                    textField.requestFocusInWindow();
                    return true;
                }
            } else if (comp instanceof Container) {
                if (findAndFocusSearchField((Container) comp)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Helper method to find and click a back or close button
     */
    private static boolean findAndClickBackButton(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                String text = button.getText();
                if (text != null && (text.contains("Back") || text.contains("Cancel") || text.contains("Close"))) {
                    button.doClick();
                    return true;
                }
            } else if (comp instanceof Container) {
                if (findAndClickBackButton((Container) comp)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Helper method to find and click a save button
     */
    private static boolean findAndClickSaveButton(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                String text = button.getText();
                if (text != null && (text.contains("Save") || text.contains("Update") || text.contains("Apply"))) {
                    button.doClick();
                    return true;
                }
            } else if (comp instanceof Container) {
                if (findAndClickSaveButton((Container) comp)) {
                    return true;
                }
            }
        }
        return false;
    }
} 