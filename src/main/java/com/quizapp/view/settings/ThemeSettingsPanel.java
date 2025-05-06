package com.quizapp.view.settings;

import com.quizapp.util.ThemeManager;
import com.quizapp.view.common.Toast;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for configuring application theme settings
 */
public class ThemeSettingsPanel extends JPanel {

    private JFrame parentFrame;
    private JComboBox<String> themeComboBox;
    private JCheckBox animationsCheckbox;
    private JPanel previewPanel;
    
    /**
     * Creates a new theme settings panel
     * 
     * @param parentFrame The parent frame
     */
    public ThemeSettingsPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create title
        JLabel titleLabel = new JLabel("Theme Settings");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Theme selector
        JPanel themeSelectorPanel = createThemeSelectorPanel();
        contentPanel.add(themeSelectorPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Animation settings
        JPanel animationPanel = createAnimationPanel();
        contentPanel.add(animationPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Theme preview
        previewPanel = createPreviewPanel();
        contentPanel.add(previewPanel);
        
        // Add scroll pane for the content
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(e -> resetToDefaults());
        
        JButton applyButton = new JButton("Apply Theme");
        applyButton.addActionListener(e -> applyTheme());
        
        buttonsPanel.add(resetButton);
        buttonsPanel.add(applyButton);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        // Load initial values
        loadSettings();
        
        // Update preview when theme selection changes
        themeComboBox.addActionListener(e -> updatePreview());
    }
    
    /**
     * Creates the theme selector panel
     * 
     * @return The theme selector panel
     */
    private JPanel createThemeSelectorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Select Theme"));
        
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel themeLabel = new JLabel("Theme:");
        
        // Get theme display names
        String[] themeNames = new String[ThemeManager.Theme.values().length];
        for (int i = 0; i < themeNames.length; i++) {
            themeNames[i] = ThemeManager.Theme.values()[i].getDisplayName();
        }
        
        themeComboBox = new JComboBox<>(themeNames);
        
        comboPanel.add(themeLabel);
        comboPanel.add(themeComboBox);
        
        JTextArea descriptionArea = new JTextArea(
            "Choose from various themes to customize the appearance of the application. " +
            "Some themes are optimized for specific lighting conditions or user preferences."
        );
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.PLAIN, 12f));
        
        panel.add(comboPanel, BorderLayout.NORTH);
        panel.add(descriptionArea, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the animation settings panel
     * 
     * @return The animation panel
     */
    private JPanel createAnimationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Animation Settings"));
        
        animationsCheckbox = new JCheckBox("Enable UI Animations");
        animationsCheckbox.setToolTipText("Toggle smooth transitions and animations");
        
        JTextArea descriptionArea = new JTextArea(
            "UI animations provide smooth transitions between different states and views. " +
            "Disabling animations may improve performance on older devices or reduce motion " +
            "for users sensitive to animation."
        );
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.PLAIN, 12f));
        
        panel.add(animationsCheckbox, BorderLayout.NORTH);
        panel.add(descriptionArea, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the theme preview panel
     * 
     * @return The preview panel
     */
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Theme Preview"));
        
        JPanel mockupPanel = new JPanel();
        mockupPanel.setLayout(new BorderLayout(5, 5));
        mockupPanel.setPreferredSize(new Dimension(0, 250));
        
        // Mock header with modern styling
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel titleLabel = new JLabel("Quiz Application");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Mock toolbar with modern buttons
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton homeBtn = new JButton("Home");
        JButton quizBtn = new JButton("Quiz");
        JButton profileBtn = new JButton("Profile");
        homeBtn.setPreferredSize(new Dimension(80, 30));
        quizBtn.setPreferredSize(new Dimension(80, 30));
        profileBtn.setPreferredSize(new Dimension(80, 30));
        toolbarPanel.add(homeBtn);
        toolbarPanel.add(quizBtn);
        toolbarPanel.add(profileBtn);
        
        // Mock content with modern card layout
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Question card
        JPanel questionCard = new JPanel(new BorderLayout(10, 10));
        questionCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel questionLabel = new JLabel("Sample Question: What is the capital of France?");
        questionLabel.setFont(questionLabel.getFont().deriveFont(Font.BOLD, 14f));
        
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 8));
        JRadioButton option1 = new JRadioButton("London");
        JRadioButton option2 = new JRadioButton("Paris");
        JRadioButton option3 = new JRadioButton("Berlin");
        JRadioButton option4 = new JRadioButton("Madrid");
        optionsPanel.add(option1);
        optionsPanel.add(option2);
        optionsPanel.add(option3);
        optionsPanel.add(option4);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitBtn = new JButton("Submit");
        submitBtn.setPreferredSize(new Dimension(100, 35));
        buttonPanel.add(submitBtn);
        
        questionCard.add(questionLabel, BorderLayout.NORTH);
        questionCard.add(optionsPanel, BorderLayout.CENTER);
        questionCard.add(buttonPanel, BorderLayout.SOUTH);
        
        // Progress indicator
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel progressLabel = new JLabel("Question 1 of 10");
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN, 12f));
        progressPanel.add(progressLabel);
        
        // Assemble mockup
        contentPanel.add(questionCard, BorderLayout.CENTER);
        contentPanel.add(progressPanel, BorderLayout.SOUTH);
        
        mockupPanel.add(headerPanel, BorderLayout.NORTH);
        mockupPanel.add(toolbarPanel, BorderLayout.CENTER);
        mockupPanel.add(contentPanel, BorderLayout.SOUTH);
        
        panel.add(mockupPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Loads current settings into the UI
     */
    private void loadSettings() {
        // Set theme combo box
        ThemeManager.Theme currentTheme = ThemeManager.getCurrentTheme();
        themeComboBox.setSelectedIndex(currentTheme.ordinal());
        
        // Set animations checkbox (default to disabled)
        animationsCheckbox.setSelected(ThemeManager.areAnimationsEnabled());
        
        // Update preview
        updatePreview();
    }
    
    /**
     * Updates the preview panel based on selected theme
     */
    private void updatePreview() {
        int selectedIndex = themeComboBox.getSelectedIndex();
        ThemeManager.Theme previewTheme = ThemeManager.Theme.values()[selectedIndex];
        
        // Update preview colors based on selected theme
        updatePreviewColors(previewTheme);
    }
    
    /**
     * Updates the preview panel colors based on the selected theme
     * 
     * @param theme The theme to preview
     */
    private void updatePreviewColors(ThemeManager.Theme theme) {
        Color background, foreground, buttonBackground, buttonForeground;
        
        switch (theme) {
            case DARK:
                background = new Color(43, 43, 43);
                foreground = new Color(220, 220, 220);
                buttonBackground = new Color(60, 63, 65);
                buttonForeground = new Color(220, 220, 220);
                break;
            case INTELLIJ:
                background = new Color(240, 240, 240);
                foreground = new Color(0, 0, 0);
                buttonBackground = new Color(214, 217, 222);
                buttonForeground = new Color(0, 0, 0);
                break;
            case DARCULA:
                background = new Color(60, 63, 65);
                foreground = new Color(187, 187, 187);
                buttonBackground = new Color(77, 80, 82);
                buttonForeground = new Color(187, 187, 187);
                break;
            case ARC_LIGHT:
                background = new Color(242, 242, 242);
                foreground = new Color(46, 52, 54);
                buttonBackground = new Color(230, 230, 230);
                buttonForeground = new Color(46, 52, 54);
                break;
            case ARC_DARK:
                background = new Color(45, 45, 45);
                foreground = new Color(211, 215, 207);
                buttonBackground = new Color(60, 60, 60);
                buttonForeground = new Color(211, 215, 207);
                break;
            case LIGHT:
            default:
                background = new Color(240, 240, 240);
                foreground = new Color(0, 0, 0);
                buttonBackground = new Color(214, 217, 222);
                buttonForeground = new Color(0, 0, 0);
                break;
        }
        
        // Update preview panel components
        updateComponentColors(previewPanel, background, foreground, buttonBackground, buttonForeground);
    }
    
    /**
     * Updates a component and its children with the specified colors
     * 
     * @param component The component to update
     * @param background The background color
     * @param foreground The foreground color
     * @param buttonBackground The button background color
     * @param buttonForeground The button foreground color
     */
    private void updateComponentColors(Component component, Color background, Color foreground, 
                                      Color buttonBackground, Color buttonForeground) {
        if (component instanceof JPanel) {
            component.setBackground(background);
            component.setForeground(foreground);
        } else if (component instanceof JButton) {
            component.setBackground(buttonBackground);
            component.setForeground(buttonForeground);
        } else if (component instanceof JLabel || 
                  component instanceof JRadioButton || 
                  component instanceof JCheckBox) {
            component.setForeground(foreground);
        }
        
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                updateComponentColors(child, background, foreground, buttonBackground, buttonForeground);
            }
        }
    }
    
    /**
     * Resets settings to default values
     */
    private void resetToDefaults() {
        themeComboBox.setSelectedIndex(0); // Light theme
        animationsCheckbox.setSelected(false); // Animations disabled by default
        updatePreview();
        
        Toast.info(this, "Theme settings reset to defaults");
    }
    
    /**
     * Applies the selected theme
     */
    private void applyTheme() {
        // Get selected theme
        int selectedIndex = themeComboBox.getSelectedIndex();
        ThemeManager.Theme selectedTheme = ThemeManager.Theme.values()[selectedIndex];
        
        // Apply theme
        ThemeManager.setTheme(selectedTheme, parentFrame);
        
        // Apply animations setting
        boolean animationsEnabled = animationsCheckbox.isSelected();
        ThemeManager.setAnimationsEnabled(animationsEnabled);
        
        // Save settings
        ThemeManager.saveSettings();
        
        Toast.success(this, "Theme applied successfully");
    }
} 