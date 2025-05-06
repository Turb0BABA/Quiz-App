package com.quizapp.view.settings;

import com.quizapp.util.KeyboardShortcutManager;
import com.quizapp.util.AccessibilityManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Dialog for application settings including themes and accessibility
 */
public class SettingsDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private ThemeSettingsPanel themePanel;
    private AccessibilitySettingsPanel accessibilityPanel;
    
    /**
     * Creates a new settings dialog
     * 
     * @param parent The parent frame
     */
    public SettingsDialog(JFrame parent) {
        super(parent, "Application Settings", true);
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        
        // Create tabbed pane for different settings sections
        tabbedPane = new JTabbedPane();
        
        // Create and add theme settings panel
        themePanel = new ThemeSettingsPanel(parent);
        tabbedPane.addTab("Appearance", null, themePanel, "Configure application themes and appearance");
        
        // Create and add accessibility settings panel
        accessibilityPanel = new AccessibilitySettingsPanel(parent);
        tabbedPane.addTab("Accessibility", null, accessibilityPanel, "Configure accessibility options");
        
        // Create close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        // Add components to dialog
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set accessible descriptions
        AccessibilityManager.setAccessibleDescription(tabbedPane, 
            "Tabbed pane for different settings categories");
        AccessibilityManager.setAccessibleDescription(closeButton, 
            "Close button to dismiss the settings dialog");
        
        // Register keyboard shortcuts
        registerKeyboardShortcuts();
    }
    
    /**
     * Registers keyboard shortcuts for the dialog
     */
    private void registerKeyboardShortcuts() {
        // Register Escape key to close dialog
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
        
        // Register Ctrl+1, Ctrl+2, etc. to switch tabs
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            final int tabIndex = i;
            KeyStroke tabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, KeyEvent.CTRL_DOWN_MASK);
            Action tabAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tabbedPane.setSelectedIndex(tabIndex);
                }
            };
            
            String actionKey = "SELECT_TAB_" + (i + 1);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(tabKeyStroke, actionKey);
            getRootPane().getActionMap().put(actionKey, tabAction);
        }
    }
    
    /**
     * Opens the settings dialog and shows the specified tab
     * 
     * @param parent The parent frame
     * @param tabIndex The index of the tab to show
     * @return The settings dialog
     */
    public static SettingsDialog showDialog(JFrame parent, int tabIndex) {
        SettingsDialog dialog = new SettingsDialog(parent);
        if (tabIndex >= 0 && tabIndex < dialog.tabbedPane.getTabCount()) {
            dialog.tabbedPane.setSelectedIndex(tabIndex);
        }
        dialog.setVisible(true);
        return dialog;
    }
    
    /**
     * Opens the settings dialog
     * 
     * @param parent The parent frame
     * @return The settings dialog
     */
    public static SettingsDialog showDialog(JFrame parent) {
        return showDialog(parent, 0);
    }
} 