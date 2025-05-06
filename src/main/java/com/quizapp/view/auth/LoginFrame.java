package com.quizapp.view.auth;

import com.quizapp.dao.UserDAO;
import com.quizapp.model.User;
import com.quizapp.util.SessionManager;
import com.quizapp.view.main.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import com.quizapp.service.AuthService;
import com.quizapp.service.SessionService;
import com.quizapp.util.UIConstants;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.awt.Desktop;
import java.net.URI;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.Optional;
import java.util.Properties;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern Login Frame with enhanced UI and auto sign-in functionality
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckbox;
    private JButton loginButton;
    private JButton registerButton;
    private JButton googleSignInButton;
    private final UserDAO userDAO;
    
    // Color scheme
    private Color primaryColor = new Color(66, 133, 244);  // Google blue
    private Color secondaryColor = new Color(52, 168, 83); // Google green
    private Color accentColor = new Color(234, 67, 53);    // Google red
    private Color neutralColor = new Color(251, 188, 5);   // Google yellow
    private Color backgroundColor = new Color(248, 249, 250);
    private Color textColor = new Color(60, 64, 67);
    private Color cardColor = Color.WHITE;
    
    // Fonts
    private Font regularFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font boldFont = new Font("Segoe UI", Font.BOLD, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);
    private Font subtitleFont = new Font("Segoe UI", Font.PLAIN, 16);

    public LoginFrame() {
        this.userDAO = new UserDAO();
        
        setTitle("Quiz Application - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        // Use a layered approach for better design
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Paint gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(240, 242, 245),
                    0, getHeight(), new Color(225, 232, 240)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle pattern or texture if desired
                g2d.setColor(new Color(255, 255, 255, 30));
                for (int i = 0; i < getHeight(); i += 15) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
                
                g2d.dispose();
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        
        // Create main card panel with drop shadow
        JPanel mainCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw card shadow
                int shadowSize = 15;
                for (int i = 0; i < shadowSize; i++) {
                    int alpha = 10 - i * (10 / shadowSize);
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.setStroke(new BasicStroke(i * 0.5f));
                    g2d.draw(new RoundRectangle2D.Float(
                        shadowSize - i, shadowSize - i, 
                        getWidth() - 2 * (shadowSize - i), 
                        getHeight() - 2 * (shadowSize - i), 
                        15, 15
                    ));
                }
                
                // Draw card background
                g2d.setColor(cardColor);
                g2d.fill(new RoundRectangle2D.Float(
                    shadowSize, shadowSize,
                    getWidth() - 2 * shadowSize,
                    getHeight() - 2 * shadowSize,
                    15, 15
                ));
                
                g2d.dispose();
            }
        };
        mainCard.setOpaque(false);
        mainCard.setLayout(new BorderLayout());
        mainCard.setPreferredSize(new Dimension(400, 580));
        
        // Content panel with padding
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));
        contentPanel.setOpaque(false);
        
        // App logo and name
        JPanel logoPanel = createLogoPanel();
        
        // Login header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        
        JLabel headerLabel = new JLabel("Welcome back");
        headerLabel.setFont(headerFont);
        headerLabel.setForeground(textColor);
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Continue your learning journey");
        subtitleLabel.setFont(subtitleFont);
        subtitleLabel.setForeground(new Color(95, 99, 104));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        headerPanel.add(headerLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);
        
        // Form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Username field with floating label effect
        JPanel usernamePanel = createFloatingLabelField("Username or Email", false);
        usernameField = (JTextField) usernamePanel.getClientProperty("inputField");
        
        // Password field with floating label effect
        JPanel passwordPanel = createFloatingLabelField("Password", true);
        passwordField = (JPasswordField) passwordPanel.getClientProperty("inputField");

        // Remember me checkbox
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));
        
        JPanel rememberPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rememberPanel.setOpaque(false);
        
        rememberMeCheckbox = new JCheckBox("Auto sign in");
        rememberMeCheckbox.setFont(regularFont);
        rememberMeCheckbox.setOpaque(false);
        rememberMeCheckbox.setForeground(textColor);
        rememberMeCheckbox.setFocusPainted(false);
        
        rememberPanel.add(rememberMeCheckbox);
        
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotPanel.setOpaque(false);
        
        JLabel forgotPassword = new JLabel("Forgot password?");
        forgotPassword.setFont(regularFont);
        forgotPassword.setForeground(primaryColor);
        forgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        forgotPanel.add(forgotPassword);
        
        optionsPanel.add(rememberPanel, BorderLayout.WEST);
        optionsPanel.add(forgotPanel, BorderLayout.EAST);

        // Sign-in button
        loginButton = createStyledButton("Sign In", primaryColor);
        loginButton.addActionListener(e -> handleLogin());
        
        // Divider
        JPanel dividerPanel = new JPanel(new BorderLayout(10, 0));
        dividerPanel.setOpaque(false);
        dividerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JSeparator leftSep = new JSeparator();
        leftSep.setForeground(new Color(220, 220, 220));
        
        JLabel orLabel = new JLabel("OR");
        orLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        orLabel.setForeground(new Color(120, 120, 120));
        orLabel.setHorizontalAlignment(SwingConstants.CENTER);
        orLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        JSeparator rightSep = new JSeparator();
        rightSep.setForeground(new Color(220, 220, 220));
        
        dividerPanel.add(leftSep, BorderLayout.WEST);
        dividerPanel.add(orLabel, BorderLayout.CENTER);
        dividerPanel.add(rightSep, BorderLayout.EAST);
        
        // Google Sign-in button
        googleSignInButton = createSocialButton("Sign in with", createGoogleIcon());
        googleSignInButton.addActionListener(e -> handleGoogleSignIn());
        
        // Add an error message area
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setOpaque(false);
        errorPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        errorPanel.setVisible(false); // Initially hidden
        
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(accentColor);
        errorLabel.setFont(regularFont);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setIcon(createErrorIcon());
        errorLabel.setIconTextGap(10);
        
        errorPanel.add(errorLabel, BorderLayout.CENTER);
        
        // Store as property for access in error handling methods
        errorPanel.putClientProperty("errorLabel", errorLabel);
        
        // Register button with improved actionable design
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new BoxLayout(registerPanel, BoxLayout.Y_AXIS));
        registerPanel.setOpaque(false);
        registerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JSeparator registerSep = new JSeparator();
        registerSep.setForeground(new Color(230, 230, 230));
        registerSep.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerSep.setMaximumSize(new Dimension(330, 1));
        
        JPanel registerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        registerButtonPanel.setOpaque(false);
        registerButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        registerButtonPanel.setMaximumSize(new Dimension(330, 30));
        
        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setFont(regularFont);
        noAccountLabel.setForeground(textColor);
        
        registerButton = new JButton("Sign up");
        registerButton.setFont(boldFont);
        registerButton.setForeground(primaryColor);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setMargin(new Insets(0, 0, 0, 0));
        registerButton.addActionListener(e -> handleRegister());
        
        // Add hover effect to sign up button
        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerButton.setForeground(new Color(25, 103, 210)); // Darker blue
                registerButton.setText("<html><u>Sign up</u></html>"); // Underline on hover
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                registerButton.setForeground(primaryColor);
                registerButton.setText("Sign up");
            }
        });
        
        registerButtonPanel.add(noAccountLabel);
        registerButtonPanel.add(Box.createHorizontalStrut(5));
        registerButtonPanel.add(registerButton);
        
        registerPanel.add(registerSep);
        registerPanel.add(registerButtonPanel);
        
        // Add components to form panel
        formPanel.add(usernamePanel);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(passwordPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(optionsPanel);
        formPanel.add(loginButton);
        formPanel.add(errorPanel); // Add error panel below login button
        formPanel.add(dividerPanel);
        formPanel.add(googleSignInButton);
        formPanel.add(registerPanel);
        
        // Store error panel for access in error handling
        formPanel.putClientProperty("errorPanel", errorPanel);
        
        // Add all sections to content panel
        contentPanel.add(logoPanel);
        contentPanel.add(headerPanel);
        contentPanel.add(formPanel);
        
        // Add content to card
        mainCard.add(contentPanel, BorderLayout.CENTER);
        
        // Add card to background
        backgroundPanel.add(mainCard);
        
        // Add background to frame
        setContentPane(backgroundPanel);
        
        // Check for remembered login
        checkRememberedLogin();
    }
    
    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setOpaque(false);
        
        // Create a more polished logo
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circular logo with Google-inspired color scheme
                int size = 48;
                int[] colors = {primaryColor.getRGB(), secondaryColor.getRGB(), 
                                accentColor.getRGB(), neutralColor.getRGB()};
                
                // Draw colored quadrants
                for (int i = 0; i < 4; i++) {
                    g2d.setColor(new Color(colors[i]));
                    g2d.fillArc(0, 0, size, size, i * 90, 90);
                }
                
                // White center for the "Q"
                g2d.setColor(Color.WHITE);
                g2d.fillOval(size/4, size/4, size/2, size/2);
                
                // Draw "Q" in the center
                g2d.setColor(textColor);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString("Q", (size - fm.stringWidth("Q")) / 2, 
                               ((size - fm.getHeight()) / 2) + fm.getAscent());
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(48, 48);
            }
        };
        
        JLabel appNameLabel = new JLabel("Quiz Application");
        appNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appNameLabel.setForeground(textColor);
        
        logoPanel.add(logoIcon);
        logoPanel.add(Box.createHorizontalStrut(10));
        logoPanel.add(appNameLabel);
        
        return logoPanel;
    }
    
    private JPanel createFloatingLabelField(String labelText, boolean isPassword) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(330, 70));
        panel.setPreferredSize(new Dimension(330, 70));
        
        // Create the field
        JTextField field;
        if (isPassword) {
            field = new JPasswordField();
        } else {
            field = new JTextField();
        }
        field.setFont(regularFont);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(210, 210, 210)),
            BorderFactory.createEmptyBorder(5, 30, 5, isPassword ? 30 : 5) // Padding for icon
        ));
        field.setOpaque(false);
        
        // Create the floating label
        JLabel label = new JLabel(labelText);
        label.setFont(regularFont);
        label.setForeground(new Color(120, 120, 120));
        
        // Panel to hold the label
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0)); // Align with icon
        labelPanel.setOpaque(false);
        labelPanel.add(label);
        
        // Create field wrapper panel for the icon and field
        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setOpaque(false);
        
        // Add icon based on field type
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(30, field.getPreferredSize().height));
        
        // Use appropriate icon
        if (isPassword) {
            iconLabel.setIcon(createPasswordIcon());
        } else {
            iconLabel.setIcon(createUserIcon());
        }
        
        fieldWrapper.add(iconLabel, BorderLayout.WEST);
        fieldWrapper.add(field, BorderLayout.CENTER);
        
        // Add password visibility toggle if it's a password field
        if (isPassword) {
            JPanel togglePanel = new JPanel(new BorderLayout());
            togglePanel.setOpaque(false);
            togglePanel.setPreferredSize(new Dimension(30, field.getPreferredSize().height));
            
            JLabel visibilityToggle = new JLabel(createEyeIcon(false));
            visibilityToggle.setHorizontalAlignment(SwingConstants.CENTER);
            visibilityToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Toggle password visibility on click
            visibilityToggle.addMouseListener(new MouseAdapter() {
                private boolean visible = false;
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    visible = !visible;
                    JPasswordField passField = (JPasswordField) field;
                    if (visible) {
                        passField.setEchoChar((char) 0); // Show password
                        visibilityToggle.setIcon(createEyeIcon(true));
                    } else {
                        passField.setEchoChar('•'); // Hide password
                        visibilityToggle.setIcon(createEyeIcon(false));
                    }
                }
            });
            
            togglePanel.add(visibilityToggle, BorderLayout.CENTER);
            fieldWrapper.add(togglePanel, BorderLayout.EAST);
        }
        
        panel.add(labelPanel, BorderLayout.NORTH);
        panel.add(fieldWrapper, BorderLayout.CENTER);
        
        // Store the field as a client property for easy access
        panel.putClientProperty("inputField", field);
        
        // Add focus listeners for floating label effect
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                label.setForeground(primaryColor);
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, primaryColor),
                    BorderFactory.createEmptyBorder(5, 30, 5, isPassword ? 30 : 5)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                label.setForeground(new Color(120, 120, 120));
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(210, 210, 210)),
                    BorderFactory.createEmptyBorder(5, 30, 5, isPassword ? 30 : 5)
                ));
            }
        });
        
        return panel;
    }
    
    // Method to create a user icon
    private ImageIcon createUserIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw user icon
        g2d.setColor(new Color(120, 120, 120));
        // Head
        g2d.fillOval(5, 2, 6, 6);
        // Body
        g2d.fillRoundRect(3, 8, 10, 7, 4, 4);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    // Method to create a password/lock icon
    private ImageIcon createPasswordIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw lock icon
        g2d.setColor(new Color(120, 120, 120));
        // Lock body
        g2d.fillRoundRect(3, 7, 10, 8, 2, 2);
        // Lock shackle
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawArc(4, 2, 8, 8, 0, 180);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    // Method to create eye icon for password visibility toggle
    private ImageIcon createEyeIcon(boolean crossed) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw eye icon
        g2d.setColor(new Color(120, 120, 120));
        g2d.setStroke(new BasicStroke(1.5f));
        // Eye shape
        g2d.drawArc(2, 6, 12, 6, 0, 180);
        g2d.drawArc(2, 6, 12, 6, 180, 180);
        // Pupil
        g2d.fillOval(7, 7, 2, 2);
        
        // If crossed (not visible), draw a line through the eye
        if (crossed) {
            g2d.setColor(new Color(200, 80, 80));
            g2d.drawLine(3, 3, 13, 13);
        }
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    private JButton createStyledButton(String text, Color baseColor) {
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
                    g2d.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    // Slightly brighter when hovered
                    g2d.setColor(baseColor.brighter());
                } else {
                    g2d.setColor(baseColor);
                }
                
                g2d.fillRoundRect(0, 0, width, height, 10, 10);
                
                // Add a subtle gradient
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 50),
                    0, height, new Color(255, 255, 255, 0)
                );
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, width, height/2, 10, 10);
                
                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(boldFont);
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
        button.setPreferredSize(new Dimension(330, 45));
        button.setMaximumSize(new Dimension(330, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private JButton createSocialButton(String text, Image icon) {
        JButton button = new JButton(text);
        button.setIcon(new ImageIcon(icon));
        button.setFont(boldFont);
        button.setForeground(textColor);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(8);
        
        // Add light background and border
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Center the icon and text
        button.setHorizontalAlignment(SwingConstants.CENTER);
        
        button.setPreferredSize(new Dimension(330, 45));
        button.setMaximumSize(new Dimension(330, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(245, 245, 245));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(primaryColor, 1, true),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
        
        return button;
    }

    /**
     * Checks if there's a remembered user and auto-fills credentials
     */
    private void checkRememberedLogin() {
        // Check if we have a remembered user session
        if (SessionService.getInstance().hasValidSession()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // Show loading indication on the login button
            loginButton.setEnabled(false);
            loginButton.setText("Signing in...");
            
            // Use SwingWorker for background processing
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                private User authenticatedUser;
                
                @Override
                protected Boolean doInBackground() throws Exception {
                    // Try to restore the session
                    boolean restored = SessionService.getInstance().restoreSession();
                    
                    if (restored) {
                        // Get the current user from either service
                        authenticatedUser = SessionService.getInstance().getCurrentUser();
                        if (authenticatedUser == null) {
                            authenticatedUser = AuthService.getInstance().getCurrentUser();
                        }
                        
                        // Update last login time
                        if (authenticatedUser != null) {
                            userDAO.updateLastLogin(authenticatedUser.getUserId());
                            return true;
                        }
                    }
                    return false;
                }
                
                @Override
                protected void done() {
                    try {
                        if (get() && authenticatedUser != null) {
                            // Auto-login successful
                            System.out.println("Auto-login successful for user: " + authenticatedUser.getUsername());
                            
                            // Launch main application
                            dispose();
                            new MainFrame(authenticatedUser.getUserId(), 
                                         authenticatedUser.isAdmin(), 
                                         authenticatedUser.getUsername()).setVisible(true);
                        } else {
                            // Reset UI to normal state
                            loginButton.setEnabled(true);
                            loginButton.setText("Sign In");
                            setCursor(Cursor.getDefaultCursor());
                            
                            // Set checkbox based on remembered state
                            rememberMeCheckbox.setSelected(SessionService.getInstance().hasValidSession());
                        }
                    } catch (Exception e) {
                        // Handle any errors
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");
                        setCursor(Cursor.getDefaultCursor());
                        System.err.println("Error during auto-login: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            };
            
            worker.execute();
        }
    }

    private void handleLogin() {
        final String username = usernameField.getText();
        // Handle placeholder text
        final String usernameToUse;
        if (username.equals("Username, Email or Phone")) {
            usernameToUse = "";
        } else {
            usernameToUse = username;
        }
        
        final String password = new String(passwordField.getPassword());
        final boolean rememberMe = rememberMeCheckbox.isSelected();

        // Get the error panel and label
        JPanel errorPanel = (JPanel) ((JPanel) loginButton.getParent()).getClientProperty("errorPanel");
        JLabel errorLabel = (JLabel) errorPanel.getClientProperty("errorLabel");
        
        // Hide any previous error
        errorPanel.setVisible(false);

        if (usernameToUse.isEmpty() || password.isEmpty()) {
            // Show inline error instead of popup
            errorLabel.setText("Please enter both username and password");
            errorPanel.setVisible(true);
            
            // Highlight empty fields
            if (usernameToUse.isEmpty()) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, accentColor),
                    BorderFactory.createEmptyBorder(5, 30, 5, 5)
                ));
            }
            if (password.isEmpty()) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, accentColor),
                    BorderFactory.createEmptyBorder(5, 30, 5, 30)
                ));
            }
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Use SwingWorker for background authentication
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private User authenticatedUser = null;
            
            @Override
            protected Boolean doInBackground() throws Exception {
                // Use the SessionService for authentication
                boolean success = SessionService.getInstance().login(usernameToUse, password, rememberMe);
                
                if (success) {
                    // Get the authenticated user from either service
                    authenticatedUser = SessionService.getInstance().getCurrentUser();
                    if (authenticatedUser == null) {
                        authenticatedUser = AuthService.getInstance().getCurrentUser();
                    }
                    return true;
                }
                return false;
            }
            
            @Override
            protected void done() {
                try {
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                    setCursor(Cursor.getDefaultCursor());
                    
                    // Get the error panel and label
                    JPanel errorPanel = (JPanel) ((JPanel) loginButton.getParent()).getClientProperty("errorPanel");
                    JLabel errorLabel = (JLabel) errorPanel.getClientProperty("errorLabel");
                    
                    if (get() && authenticatedUser != null) {
                        // Show success message if needed
                        System.out.println("Login successful for user: " + authenticatedUser.getUsername());
                        
                        // Launch main application
                dispose();
                        new MainFrame(authenticatedUser.getUserId(), 
                                    authenticatedUser.isAdmin(), 
                                    authenticatedUser.getUsername()).setVisible(true);
            } else {
                        // Show error inline instead of dialog
                        errorLabel.setText("Invalid username or password");
                        errorPanel.setVisible(true);
                        
                        // Add helpful hint about admin login
                        if (usernameField.getText().isEmpty() || 
                            (!usernameField.getText().equals("admin") && 
                             usernameField.getText().indexOf('@') == -1)) {
                            errorLabel.setText("Invalid username or password. Try admin/admin123");
                        }
                    }
                } catch (Exception e) {
                    setCursor(Cursor.getDefaultCursor());
                    
                    // Get the error panel and label
                    JPanel errorPanel = (JPanel) ((JPanel) loginButton.getParent()).getClientProperty("errorPanel");
                    JLabel errorLabel = (JLabel) errorPanel.getClientProperty("errorLabel");
                    
                    // Show error inline
                    errorLabel.setText("Error: " + e.getMessage());
                    errorPanel.setVisible(true);
                    e.printStackTrace();
        }
            }
        };
        
        worker.execute();
    }

    private void handleRegister() {
        // Open the registration dialog
        RegisterDialog registerDialog = new RegisterDialog(this);
        registerDialog.setVisible(true);
    }

    private void handleGoogleSignIn() {
        // Load OAuth credentials
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("oauth.properties")) {
            if (in == null) {
                JOptionPane.showMessageDialog(this, "Missing oauth.properties file.", "Google SSO", JOptionPane.ERROR_MESSAGE);
                return;
            }
            props.load(in);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading OAuth config: " + ex.getMessage(), "Google SSO", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final String clientId = props.getProperty("clientId");
        final String clientSecret = props.getProperty("clientSecret");
        final String redirectUri = props.getProperty("redirectUri");
        if (clientId == null || clientSecret == null || redirectUri == null || clientId.contains("YOUR_GOOGLE_CLIENT_ID")) {
            JOptionPane.showMessageDialog(this, "Please set your Google OAuth Client ID and Secret in oauth.properties.", "Google SSO", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Build OAuth2 service
        final OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .defaultScope("profile email")
                .callback(redirectUri)
                .build(GoogleApi20.instance());
        String authUrl = service.getAuthorizationUrl();
        // Open browser for user authentication
        try {
            Desktop.getDesktop().browse(new URI(authUrl));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to open browser: " + ex.getMessage(), "Google SSO", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Prompt user to paste the code
        String code = JOptionPane.showInputDialog(this, "After authorizing, paste the code from the browser here:", "Google SSO", JOptionPane.PLAIN_MESSAGE);
        if (code == null || code.isEmpty()) return;
        // Exchange code for access token
        final String finalCode = code; // Make code effectively final for use in lambda
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                OAuth2AccessToken accessToken = service.getAccessToken(finalCode);
                // Get user info from Google
                java.net.URL url = new java.net.URL("https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken.getAccessToken());
                try (java.io.InputStream is = url.openStream()) {
                    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                    return s.hasNext() ? s.next() : "";
                }
            }
            @Override
            protected void done() {
                try {
                    String userInfoJson = get();
                    // Parse JSON (simple parsing for email and name)
                    String email = userInfoJson.replaceAll(".*\\\"email\\\":\\\"([^\\\"]+)\\\".*", "$1");
                    String name = userInfoJson.replaceAll(".*\\\"name\\\":\\\"([^\\\"]+)\\\".*", "$1");
                    if (email == null || email.isEmpty()) {
                        JOptionPane.showMessageDialog(LoginFrame.this, "Failed to retrieve email from Google.", "Google SSO", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Check if user exists, if not, auto-register
                    Optional<User> userOpt = userDAO.findByEmail(email);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        // Login the user automatically
                        dispose();
                        new MainFrame(user.getUserId(), user.isAdmin(), user.getUsername()).setVisible(true);
                    } else {
                        // Auto-register the user
                        String username = email.split("@")[0];
                        String defaultPassword = "googleauth-" + System.currentTimeMillis();
                        User newUser = new User();
                        newUser.setUsername(username);
                        newUser.setEmail(email);
                        newUser.setPassword(defaultPassword);
                        newUser.setFullName(name);
                        // Set other required fields
                        userDAO.create(newUser);
                        
                        // Login the new user automatically
                        Optional<User> createdUserOpt = userDAO.findByEmail(email);
                        if (createdUserOpt.isPresent()) {
                            User createdUser = createdUserOpt.get();
                            JOptionPane.showMessageDialog(LoginFrame.this, "Welcome! A new account has been created for you.", "Google SSO", JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                            new MainFrame(createdUser.getUserId(), createdUser.isAdmin(), createdUser.getUsername()).setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(LoginFrame.this, "Failed to create account with Google info.", "Google SSO", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Error during Google authentication: " + ex.getMessage(), "Google SSO", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Creates a Google icon image
     */
    private Image createGoogleIcon() {
        // Create a custom Google icon (G logo)
        int size = 24;  // Increased size for better visibility
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Google colors
        Color blue = new Color(66, 133, 244);
        Color red = new Color(219, 68, 55);
        Color yellow = new Color(244, 180, 0);
        Color green = new Color(15, 157, 88);
        
        // Draw background
        g2d.setColor(Color.WHITE);
        g2d.fillOval(0, 0, size, size);
        
        // Draw the "G" shape
        g2d.setColor(blue);
        g2d.fillArc(0, 0, size, size, 180, 90);
        
        g2d.setColor(red);
        g2d.fillArc(0, 0, size, size, 270, 90);
        
        g2d.setColor(yellow);
        g2d.fillArc(0, 0, size, size, 0, 90);
        
        g2d.setColor(green);
        g2d.fillArc(0, 0, size, size, 90, 90);
        
        // White center
        g2d.setColor(Color.WHITE);
        g2d.fillOval(size/4, size/4, size/2, size/2);
        
        // Blue arc for the "G" letter opening
        g2d.setColor(blue);
        g2d.fillRect(size/2, size/3, size/2, size/3);
        
        g2d.dispose();
        return image;
    }

    // Method to create error icon
    private ImageIcon createErrorIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw error icon (exclamation mark in circle)
        g2d.setColor(accentColor);
        g2d.fillOval(0, 0, 16, 16);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString("!", (16 - fm.stringWidth("!")) / 2, ((16 - fm.getHeight()) / 2) + fm.getAscent());
        
        g2d.dispose();
        return new ImageIcon(image);
    }
}