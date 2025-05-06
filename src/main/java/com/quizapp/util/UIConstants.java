package com.quizapp.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Contains constants and utility methods for consistent UI styling
 * across the application with enhanced modern design.
 */
public class UIConstants {
    // Primary theme colors - modernized palette
    public static final Color PRIMARY_COLOR = new Color(66, 133, 244);   // Google blue
    public static final Color SECONDARY_COLOR = new Color(52, 168, 83);  // Google green
    public static final Color ACCENT_COLOR = new Color(234, 67, 53);     // Google red
    public static final Color NEUTRAL_COLOR = new Color(251, 188, 5);    // Google yellow
    public static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Light gray-blue background
    public static final Color CARD_BACKGROUND = new Color(255, 255, 255);   // Pure white for cards
    public static final Color CARD_HOVER_BACKGROUND = new Color(248, 250, 252); // Slightly blue on hover
    
    // Text colors
    public static final Color TEXT_COLOR = new Color(60, 64, 67);        // Dark gray
    public static final Color TEXT_SECONDARY = new Color(95, 99, 104);   // Medium gray
    public static final Color TEXT_DISABLED = new Color(189, 193, 198);  // Light gray
    public static final Color TEXT_LINK = new Color(26, 115, 232);       // Link blue

    // Semantic colors
    public static final Color SUCCESS_COLOR = new Color(52, 168, 83);    // Green
    public static final Color WARNING_COLOR = new Color(251, 188, 5);    // Yellow
    public static final Color ERROR_COLOR = new Color(234, 67, 53);      // Red
    public static final Color INFO_COLOR = new Color(66, 133, 244);      // Blue
    
    // Folder navigation colors
    public static final Color FOLDER_MAIN_BG = new Color(232, 240, 254);  // Light blue for main folders
    public static final Color FOLDER_SUB_BG = new Color(230, 249, 231);   // Light green for subfolders
    public static final Color FOLDER_MAIN_HOVER = new Color(217, 232, 253); // Hover state for main folders
    public static final Color FOLDER_SUB_HOVER = new Color(220, 245, 220);  // Hover state for subfolders
    
    // Border and shadow constants
    public static final int BORDER_RADIUS = 8;       // Standard corner radius for components
    public static final int CARD_ELEVATION = 2;      // Card elevation level (1-5)
    public static final float SHADOW_OPACITY = 0.12f; // Shadow opacity
    
    // Padding constants
    public static final int STANDARD_PADDING = 20;
    public static final int SMALL_PADDING = 12;
    public static final int LARGE_PADDING = 32;
    public static final int CONTENT_SPACING = 16;    // Standard spacing between content elements
    
    // Font settings - using system fonts for better native feel
    public static final String PRIMARY_FONT = "Segoe UI";  // Windows
    public static final String FALLBACK_FONT = "SF Pro Display, Helvetica Neue, Arial, sans-serif"; // macOS, others
    
    // Font size constants
    public static final int TITLE_FONT_SIZE = 24;
    public static final int SUBTITLE_FONT_SIZE = 18;
    public static final int SUB_HEADER_FONT_SIZE = 16;
    public static final int BODY_FONT_SIZE = 14;
    public static final int SMALL_FONT_SIZE = 12;
    public static final int LABEL_FONT_SIZE = 14;
    
    // Standard borders
    public static Border CARD_BORDER = createCardBorder(CARD_ELEVATION);
    
    public static Border FOLDER_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(2, 2, 5, 5), // Shadow effect
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        )
    );
    
    /**
     * Creates a shadow border with elevation effect
     */
    public static Border createCardBorder(int elevation) {
        int shadowSize = Math.min(5, Math.max(1, elevation)) * 2;
        
        return BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 2, shadowSize + 2, shadowSize + 2),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
            )
        );
    }
    
    /**
     * Creates a button with modern styling
     */
    public static JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Button background
                if (getModel().isPressed()) {
                    // Darker when pressed
                    g2d.setColor(getDarkerColor(color, 0.2f));
                } else if (getModel().isRollover()) {
                    // Slightly brighter when hovered
                    g2d.setColor(getBrighterColor(color, 0.1f));
                } else {
                    g2d.setColor(color);
                }
                
                g2d.fillRoundRect(0, 0, width, height, BORDER_RADIUS, BORDER_RADIUS);
                
                // Add a subtle gradient/highlight
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 40),
                    0, height, new Color(255, 255, 255, 0)
                );
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, width, height/2, BORDER_RADIUS, BORDER_RADIUS);
                
                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(getText(), 
                    (width - fm.stringWidth(getText())) / 2, 
                    (height + fm.getAscent() - fm.getDescent()) / 2);
                
                g2d.dispose();
            }
        };
        
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(createFont(true, LABEL_FONT_SIZE));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    /**
     * Creates a text button (no background) with hover effect
     */
    public static JButton createTextButton(String text, Color textColor) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFont(createFont(true, LABEL_FONT_SIZE));
        button.setForeground(textColor);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(getDarkerColor(textColor, 0.2f));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(textColor);
            }
        });
        
        return button;
    }
    
    /**
     * Creates a button with outlined style
     */
    public static JButton createOutlinedButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background (white or very light shade of the color)
                if (getModel().isPressed()) {
                    g2d.setColor(getLighterColor(color, 0.9f));
                } else if (getModel().isRollover()) {
                    g2d.setColor(getLighterColor(color, 0.95f));
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);
                
                // Border
                g2d.setColor(color);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BORDER_RADIUS, BORDER_RADIUS);
                
                // Text
                g2d.setColor(color);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(getText(), 
                    (getWidth() - fm.stringWidth(getText())) / 2, 
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                
                g2d.dispose();
            }
        };
        
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(createFont(true, LABEL_FONT_SIZE));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    // Standardized component styling
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR);
    }
    
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, SECONDARY_COLOR);
    }
    
    public static JButton createAccentButton(String text) {
        return createStyledButton(text, ACCENT_COLOR);
    }
    
    public static JButton createBackButton() {
        JButton button = createOutlinedButton("← Back", TEXT_SECONDARY);
        return button;
    }
    
    /**
     * Creates a folder entry button with door-like visual indicators
     */
    public static JButton createDoorButton(String text, boolean isMainCategory) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                Color bgColor = isMainCategory ? FOLDER_MAIN_BG : FOLDER_SUB_BG;
                if (getModel().isPressed()) {
                    bgColor = getDarkerColor(bgColor, 0.1f);
                } else if (getModel().isRollover()) {
                    bgColor = getLighterColor(bgColor, 0.03f);
                }
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);
                
                // Door design
                Color doorColor = isMainCategory ? PRIMARY_COLOR : SECONDARY_COLOR;
                
                // Door handle
                g2d.setColor(doorColor);
                g2d.fillRoundRect(getWidth() - 18, getHeight() / 2 - 5, 5, 10, 2, 2);
                
                // Border
                g2d.setColor(doorColor);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BORDER_RADIUS, BORDER_RADIUS);
                
                // Text
                g2d.setColor(doorColor);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(getText(), 15, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                
                g2d.dispose();
            }
        };
        
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(createFont(true, LABEL_FONT_SIZE));
        button.setForeground(isMainCategory ? PRIMARY_COLOR : SECONDARY_COLOR);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        
        return button;
    }
    
    /**
     * Creates a standard header label with consistent styling
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(createFont(true, TITLE_FONT_SIZE));
        label.setForeground(TEXT_COLOR);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, STANDARD_PADDING, 0));
        return label;
    }
    
    /**
     * Creates a subtitle label with consistent styling
     */
    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(createFont(false, SUBTITLE_FONT_SIZE));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }
    
    /**
     * Creates a sub-header label with consistent styling
     */
    public static JLabel createSubHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(createFont(true, SUB_HEADER_FONT_SIZE));
        label.setForeground(TEXT_COLOR);
        return label;
    }
    
    /**
     * Creates a card panel with standard card styling (white background, shadow effect)
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                int shadowSize = 5;
                for (int i = 0; i < shadowSize; i++) {
                    float alpha = SHADOW_OPACITY - ((float)i / shadowSize * SHADOW_OPACITY);
                    g2d.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
                    g2d.drawRoundRect(
                        shadowSize - i, 
                        shadowSize - i, 
                        getWidth() - (shadowSize - i) * 2 - 1, 
                        getHeight() - (shadowSize - i) * 2 - 1, 
                        BORDER_RADIUS, 
                        BORDER_RADIUS
                    );
                }
                
                // Fill background
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRoundRect(
                    shadowSize, 
                    shadowSize, 
                    getWidth() - shadowSize * 2, 
                    getHeight() - shadowSize * 2, 
                    BORDER_RADIUS, 
                    BORDER_RADIUS
                );
                
                g2d.dispose();
            }
        };
        
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(
            shadowSize + STANDARD_PADDING, 
            shadowSize + STANDARD_PADDING, 
            shadowSize + STANDARD_PADDING, 
            shadowSize + STANDARD_PADDING
        ));
        
        return panel;
    }
    
    // Shadow size for card panels
    private static final int shadowSize = 5;
    
    /**
     * Creates a card panel with custom layout
     */
    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = createCardPanel();
        panel.setLayout(layout);
        return panel;
    }

    /**
     * Creates a breadcrumb navigation panel
     * @param path Array of path segments from root to current location
     * @return JPanel containing the breadcrumb UI
     */
    public static JPanel createBreadcrumbPanel(String[] path) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);
        
        for (int i = 0; i < path.length; i++) {
            JLabel pathLabel = new JLabel(path[i]);
            pathLabel.setFont(createFont(i == path.length - 1, SMALL_FONT_SIZE));
            pathLabel.setForeground(i == path.length - 1 ? TEXT_COLOR : TEXT_LINK);
            
            panel.add(pathLabel);
            
            if (i < path.length - 1) {
                JLabel separator = new JLabel(" › ");
                separator.setForeground(TEXT_SECONDARY);
                separator.setFont(createFont(false, SMALL_FONT_SIZE));
                panel.add(separator);
            }
        }
        
        return panel;
    }
    
    /**
     * Creates a modern progress bar
     */
    public static JProgressBar createProgressBar(int min, int max, int value) {
        JProgressBar progressBar = new JProgressBar(min, max) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw background
                g2d.setColor(new Color(232, 234, 237));
                g2d.fillRoundRect(0, 0, width, height, height, height);
                
                // Draw progress
                int progressWidth = (int) ((double) getValue() / getMaximum() * width);
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillRoundRect(0, 0, progressWidth, height, height, height);
                
                g2d.dispose();
            }
        };
        
        progressBar.setValue(value); // Set value after creating the progress bar
        progressBar.setOpaque(false);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 8));
        
        return progressBar;
    }
    
    /**
     * Adds a tooltip with improved styling
     */
    public static void addTooltip(JComponent component, String tooltipText) {
        component.setToolTipText("<html><body style='padding: 8px; font-family: " + 
                                 PRIMARY_FONT + ", " + FALLBACK_FONT + 
                                 "; font-size: " + SMALL_FONT_SIZE + "px'>" + 
                                 tooltipText + "</body></html>");
    }
    
    /**
     * Creates a font with the application's standard font family
     */
    public static Font createFont(boolean bold, int size) {
        return new Font(PRIMARY_FONT, bold ? Font.BOLD : Font.PLAIN, size);
    }
    
    /**
     * Returns a darker version of the given color
     */
    public static Color getDarkerColor(Color color, float factor) {
        return new Color(
            Math.max(0, (int)(color.getRed() * (1 - factor))),
            Math.max(0, (int)(color.getGreen() * (1 - factor))),
            Math.max(0, (int)(color.getBlue() * (1 - factor))),
            color.getAlpha()
        );
    }
    
    /**
     * Returns a brighter version of the given color
     */
    public static Color getBrighterColor(Color color, float factor) {
        return new Color(
            Math.min(255, (int)(color.getRed() * (1 + factor))),
            Math.min(255, (int)(color.getGreen() * (1 + factor))),
            Math.min(255, (int)(color.getBlue() * (1 + factor))),
            color.getAlpha()
        );
    }
    
    /**
     * Returns a lighter version of the color (more white)
     */
    public static Color getLighterColor(Color color, float whiteFactor) {
        return new Color(
            (int)(color.getRed() * (1 - whiteFactor) + 255 * whiteFactor),
            (int)(color.getGreen() * (1 - whiteFactor) + 255 * whiteFactor),
            (int)(color.getBlue() * (1 - whiteFactor) + 255 * whiteFactor),
            color.getAlpha()
        );
    }
    
    /**
     * Shows success feedback on a component
     */
    public static void showSuccessFeedback(JComponent component) {
        Color originalBg = component.getBackground();
        component.setBackground(new Color(232, 245, 233)); // Light green
        
        Timer timer = new Timer(1500, e -> component.setBackground(originalBg));
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Shows error feedback on a component
     */
    public static void showErrorFeedback(JComponent component) {
        Color originalBg = component.getBackground();
        component.setBackground(new Color(251, 233, 231)); // Light red
        
        Timer timer = new Timer(1500, e -> component.setBackground(originalBg));
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Creates a badge label (small colored indicator with text)
     */
    public static JLabel createBadgeLabel(String text, Color color) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(getLighterColor(color, 0.85f));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        
        label.setForeground(getDarkerColor(color, 0.3f));
        label.setFont(createFont(true, SMALL_FONT_SIZE));
        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        return label;
    }
} 