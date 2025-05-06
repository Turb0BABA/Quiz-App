package com.quizapp.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages keyboard shortcuts throughout the application for improved accessibility.
 */
public class KeyboardShortcutManager {
    
    private static final Map<String, KeyStroke> DEFAULT_SHORTCUTS = new HashMap<>();
    
    static {
        // Define default shortcuts
        DEFAULT_SHORTCUTS.put("logout", KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        DEFAULT_SHORTCUTS.put("toggleTheme", KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        DEFAULT_SHORTCUTS.put("viewProfile", KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        DEFAULT_SHORTCUTS.put("newQuiz", KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        DEFAULT_SHORTCUTS.put("leaderboard", KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK));
        DEFAULT_SHORTCUTS.put("adminPanel", KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        DEFAULT_SHORTCUTS.put("help", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        DEFAULT_SHORTCUTS.put("nextQuestion", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK));
        DEFAULT_SHORTCUTS.put("previousQuestion", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK));
        DEFAULT_SHORTCUTS.put("submitAnswer", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK));
        DEFAULT_SHORTCUTS.put("searchCategories", KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
    }
    
    /**
     * Registers a keyboard shortcut for a component
     * 
     * @param component The component to register the shortcut for
     * @param actionKey The unique key identifying the action
     * @param action The action to perform when the shortcut is triggered
     */
    public static void registerShortcut(JComponent component, String actionKey, Action action) {
        KeyStroke keyStroke = DEFAULT_SHORTCUTS.get(actionKey);
        if (keyStroke != null) {
            // Get the component's input map and action map
            InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = component.getActionMap();
            
            // Register the shortcut
            inputMap.put(keyStroke, actionKey);
            actionMap.put(actionKey, action);
        }
    }
    
    /**
     * Registers a keyboard shortcut for a component
     * 
     * @param component The component to register the shortcut for
     * @param actionKey The unique key identifying the action
     * @param runnable The code to run when the shortcut is triggered
     */
    public static void registerShortcut(JComponent component, String actionKey, Runnable runnable) {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        };
        registerShortcut(component, actionKey, action);
    }
    
    /**
     * Returns the KeyStroke associated with an action key
     * 
     * @param actionKey The action key
     * @return The KeyStroke or null if not found
     */
    public static KeyStroke getKeyStroke(String actionKey) {
        return DEFAULT_SHORTCUTS.get(actionKey);
    }
    
    /**
     * Returns a human-readable description of a keyboard shortcut
     * 
     * @param actionKey The action key
     * @return A human-readable string (e.g., "Ctrl+Shift+L")
     */
    public static String getShortcutText(String actionKey) {
        KeyStroke keyStroke = DEFAULT_SHORTCUTS.get(actionKey);
        if (keyStroke == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        int modifiers = keyStroke.getModifiers();
        
        if ((modifiers & KeyEvent.CTRL_DOWN_MASK) != 0) {
            sb.append("Ctrl+");
        }
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0) {
            sb.append("Shift+");
        }
        if ((modifiers & KeyEvent.ALT_DOWN_MASK) != 0) {
            sb.append("Alt+");
        }
        
        int keyCode = keyStroke.getKeyCode();
        sb.append(KeyEvent.getKeyText(keyCode));
        
        return sb.toString();
    }
    
    /**
     * Adds shortcut text to a menu item's text
     * 
     * @param menuItem The menu item
     * @param actionKey The action key for the shortcut
     */
    public static void setMenuItemShortcutText(JMenuItem menuItem, String actionKey) {
        String shortcutText = getShortcutText(actionKey);
        if (!shortcutText.isEmpty()) {
            menuItem.setText(menuItem.getText() + " (" + shortcutText + ")");
        }
    }
} 