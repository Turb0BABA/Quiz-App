package com.quizapp.view.settings;

import com.quizapp.util.AccessibilityManager;
import com.quizapp.util.ThemeManager;
import com.quizapp.util.KeyboardShortcutManager;
import com.quizapp.view.common.Toast;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * Panel for configuring accessibility settings
 */
public class AccessibilitySettingsPanel extends JPanel {
    
    private JFrame parentFrame;
    private JSlider fontSizeSlider;
    private JCheckBox highContrastCheckbox;
    private JCheckBox screenReaderOptimizedCheckbox;
    private JCheckBox animationsEnabledCheckbox;
    
    /**
     * Creates a new accessibility settings panel
     * 
     * @param parentFrame The parent frame
     */
    public AccessibilitySettingsPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create title
        JLabel titleLabel = new JLabel("Accessibility Settings");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Font size settings
        JPanel fontSizePanel = createFontSizePanel();
        contentPanel.add(fontSizePanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // High contrast mode
        JPanel highContrastPanel = createHighContrastPanel();
        contentPanel.add(highContrastPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Screen reader optimization
        JPanel screenReaderPanel = createScreenReaderPanel();
        contentPanel.add(screenReaderPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Animations
        JPanel animationsPanel = createAnimationsPanel();
        contentPanel.add(animationsPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Keyboard shortcuts info
        JPanel shortcutsPanel = createShortcutsPanel();
        contentPanel.add(shortcutsPanel);
        
        // Add scroll pane for the content
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(e -> resetToDefaults());
        
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveChanges());
        
        buttonsPanel.add(resetButton);
        buttonsPanel.add(saveButton);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        // Load initial values
        loadSettings();
        
        // Set accessibility descriptions for screen readers
        AccessibilityManager.setAccessibleDescription(this, "Panel for configuring accessibility settings");
    }
    
    /**
     * Creates the font size slider panel
     * 
     * @return The font size panel
     */
    private JPanel createFontSizePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Text Size"));
        
        JPanel sliderPanel = new JPanel(new BorderLayout(5, 5));
        
        fontSizeSlider = new JSlider(JSlider.HORIZONTAL, 75, 150, 100);
        fontSizeSlider.setMajorTickSpacing(25);
        fontSizeSlider.setPaintTicks(true);
        fontSizeSlider.setPaintLabels(true);
        
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(75, new JLabel("75%"));
        labelTable.put(100, new JLabel("100%"));
        labelTable.put(125, new JLabel("125%"));
        labelTable.put(150, new JLabel("150%"));
        fontSizeSlider.setLabelTable(labelTable);
        
        JLabel previewLabel = new JLabel("Sample Text");
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        fontSizeSlider.addChangeListener(e -> {
            int value = fontSizeSlider.getValue();
            float fontSize = previewLabel.getFont().getSize() * value / 100f;
            previewLabel.setFont(previewLabel.getFont().deriveFont(fontSize));
        });
        
        sliderPanel.add(fontSizeSlider, BorderLayout.CENTER);
        
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        
        panel.add(sliderPanel, BorderLayout.CENTER);
        panel.add(previewPanel, BorderLayout.SOUTH);
        
        AccessibilityManager.setAccessibleDescription(fontSizeSlider, 
            "Slider to adjust text size from 75% to 150% of normal size");
        
        return panel;
    }
    
    /**
     * Creates the high contrast mode panel
     * 
     * @return The high contrast panel
     */
    private JPanel createHighContrastPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Display Settings"));
        
        highContrastCheckbox = new JCheckBox("High Contrast Mode");
        highContrastCheckbox.setToolTipText("Increases color contrast for better readability");
        
        JTextArea descriptionArea = new JTextArea(
            "High contrast mode increases the color contrast between text and background " +
            "to make content more readable for people with visual impairments."
        );
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.PLAIN, 12f));
        
        panel.add(highContrastCheckbox, BorderLayout.NORTH);
        panel.add(descriptionArea, BorderLayout.CENTER);
        
        AccessibilityManager.setAccessibleDescription(highContrastCheckbox, 
            "Checkbox to enable high contrast mode for better readability");
        
        return panel;
    }
    
    /**
     * Creates the screen reader optimization panel
     * 
     * @return The screen reader panel
     */
    private JPanel createScreenReaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Screen Reader Support"));
        
        screenReaderOptimizedCheckbox = new JCheckBox("Optimize for Screen Readers");
        screenReaderOptimizedCheckbox.setToolTipText("Enhances compatibility with screen readers");
        
        JTextArea descriptionArea = new JTextArea(
            "Screen reader optimization improves compatibility with assistive technologies " +
            "by providing better descriptions and focus handling for UI elements."
        );
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.PLAIN, 12f));
        
        panel.add(screenReaderOptimizedCheckbox, BorderLayout.NORTH);
        panel.add(descriptionArea, BorderLayout.CENTER);
        
        AccessibilityManager.setAccessibleDescription(screenReaderOptimizedCheckbox, 
            "Checkbox to enable optimizations for screen readers and assistive technologies");
        
        return panel;
    }
    
    /**
     * Creates the animations panel
     * 
     * @return The animations panel
     */
    private JPanel createAnimationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Motion"));
        
        animationsEnabledCheckbox = new JCheckBox("Enable Animations");
        animationsEnabledCheckbox.setToolTipText("Toggle UI animations");
        
        JTextArea descriptionArea = new JTextArea(
            "Disabling animations may help reduce motion sickness and improve performance " +
            "on older devices. This will turn off all non-essential animations."
        );
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.PLAIN, 12f));
        
        panel.add(animationsEnabledCheckbox, BorderLayout.NORTH);
        panel.add(descriptionArea, BorderLayout.CENTER);
        
        AccessibilityManager.setAccessibleDescription(animationsEnabledCheckbox, 
            "Checkbox to enable or disable UI animations");
        
        return panel;
    }
    
    /**
     * Creates the keyboard shortcuts panel
     * 
     * @return The shortcuts panel
     */
    private JPanel createShortcutsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Keyboard Shortcuts"));
        
        String[] columnNames = {"Action", "Shortcut"};
        
        String[][] shortcutData = {
            {"Toggle Theme", KeyboardShortcutManager.getShortcutText("toggleTheme")},
            {"View Profile", KeyboardShortcutManager.getShortcutText("viewProfile")},
            {"New Quiz", KeyboardShortcutManager.getShortcutText("newQuiz")},
            {"Leaderboard", KeyboardShortcutManager.getShortcutText("leaderboard")},
            {"Admin Panel", KeyboardShortcutManager.getShortcutText("adminPanel")},
            {"Logout", KeyboardShortcutManager.getShortcutText("logout")},
            {"Help", KeyboardShortcutManager.getShortcutText("help")},
            {"Next Question", KeyboardShortcutManager.getShortcutText("nextQuestion")},
            {"Previous Question", KeyboardShortcutManager.getShortcutText("previousQuestion")},
            {"Submit Answer", KeyboardShortcutManager.getShortcutText("submitAnswer")},
            {"Search Categories", KeyboardShortcutManager.getShortcutText("searchCategories")}
        };
        
        JTable shortcutsTable = new JTable(shortcutData, columnNames);
        shortcutsTable.setRowHeight(25);
        shortcutsTable.getTableHeader().setReorderingAllowed(false);
        shortcutsTable.setEnabled(false); // Make read-only
        
        JScrollPane tableScrollPane = new JScrollPane(shortcutsTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        AccessibilityManager.setAccessibleDescription(shortcutsTable, 
            "Table of keyboard shortcuts available in the application");
        
        return panel;
    }
    
    /**
     * Loads current settings into the UI
     */
    private void loadSettings() {
        // Set font size slider
        double currentScale = ThemeManager.getFontSizeScale();
        fontSizeSlider.setValue((int) (currentScale * 100));
        
        // Set high contrast checkbox
        highContrastCheckbox.setSelected(ThemeManager.isHighContrast());
        
        // Set screen reader checkbox
        screenReaderOptimizedCheckbox.setSelected(AccessibilityManager.isScreenReaderOptimized());
        
        // Set animations checkbox
        animationsEnabledCheckbox.setSelected(ThemeManager.areAnimationsEnabled());
    }
    
    /**
     * Resets settings to default values
     */
    private void resetToDefaults() {
        fontSizeSlider.setValue(100);
        highContrastCheckbox.setSelected(false);
        screenReaderOptimizedCheckbox.setSelected(false);
        animationsEnabledCheckbox.setSelected(false);
        
        Toast.info(this, "Settings reset to defaults");
    }
    
    /**
     * Saves the current settings
     */
    private void saveChanges() {
        // Apply font size
        double fontScale = fontSizeSlider.getValue() / 100.0;
        ThemeManager.setFontSizeScale(fontScale, parentFrame);
        
        // Apply high contrast
        boolean highContrast = highContrastCheckbox.isSelected();
        ThemeManager.setHighContrast(highContrast, parentFrame);
        
        // Apply screen reader optimization
        boolean screenReaderOptimized = screenReaderOptimizedCheckbox.isSelected();
        AccessibilityManager.setScreenReaderOptimized(screenReaderOptimized, parentFrame);
        
        // Apply animations setting
        boolean animationsEnabled = animationsEnabledCheckbox.isSelected();
        ThemeManager.setAnimationsEnabled(animationsEnabled);
        
        // Save settings
        ThemeManager.saveSettings();
        AccessibilityManager.saveSettings();
        
        Toast.success(this, "Accessibility settings saved");
    }
} 