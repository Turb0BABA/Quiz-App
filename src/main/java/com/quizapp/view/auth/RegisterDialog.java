package com.quizapp.view.auth;

import com.quizapp.model.User;
import com.quizapp.dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;

/**
 * Dialog for user registration with detailed profile fields
 */
public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JButton registerButton;
    private JButton cancelButton;
    private final UserDAO userDAO;
    private Color primaryColor = new Color(80, 70, 229); // Purple similar to Casdoor
    private Color backgroundColor = new Color(246, 248, 250);
    private Color textColor = new Color(60, 60, 60);
    private Font regularFont = new Font("Arial", Font.PLAIN, 14);
    private Font boldFont = new Font("Arial", Font.BOLD, 14);

    public RegisterDialog(JFrame parent) {
        super(parent, "Create an Account", true);
        this.userDAO = new UserDAO();
        
        // Remove fixed size - we'll use pack() instead
        // setSize(450, 450);
        setMinimumSize(new Dimension(450, 500)); // Minimum size for better display
        setLocationRelativeTo(parent);
        setResizable(true); // Allow resizing for better accessibility
        
        // Create main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(backgroundColor);
        
        // Add title with icon at the top
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(backgroundColor);
        
        // Create account icon
        JLabel accountIcon = new JLabel("\uD83D\uDC64"); // Unicode user icon
        accountIcon.setFont(new Font("Arial", Font.PLAIN, 32));
        accountIcon.setForeground(primaryColor);
        
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(textColor);
        
        titlePanel.add(accountIcon);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        // Create form fields with modern styling - Use GridBagLayout for better control
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        
        // Username field
        JLabel usernameLabel = new JLabel("Username*");
        usernameLabel.setFont(regularFont);
        
        usernameField = new JTextField(20);
        usernameField.setFont(regularFont);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Email field
        JLabel emailLabel = new JLabel("Email*");
        emailLabel.setFont(regularFont);
        
        emailField = new JTextField(20);
        emailField.setFont(regularFont);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Full name field
        JLabel fullNameLabel = new JLabel("Full Name");
        fullNameLabel.setFont(regularFont);
        
        fullNameField = new JTextField(20);
        fullNameField.setFont(regularFont);
        fullNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Password field
        JLabel passwordLabel = new JLabel("Password*");
        passwordLabel.setFont(regularFont);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(regularFont);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Confirm password field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password*");
        confirmPasswordLabel.setFont(regularFont);
        
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(regularFont);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Add components to form panel with GridBagConstraints
        gbc.insets = new Insets(5, 0, 2, 0);
        
        formPanel.add(usernameLabel, gbc);
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(usernameField, gbc);
        
        gbc.insets = new Insets(5, 0, 2, 0);
        formPanel.add(emailLabel, gbc);
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(emailField, gbc);
        
        gbc.insets = new Insets(5, 0, 2, 0);
        formPanel.add(fullNameLabel, gbc);
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(fullNameField, gbc);
        
        gbc.insets = new Insets(5, 0, 2, 0);
        formPanel.add(passwordLabel, gbc);
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(passwordField, gbc);
        
        gbc.insets = new Insets(5, 0, 2, 0);
        formPanel.add(confirmPasswordLabel, gbc);
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(confirmPasswordField, gbc);
        
        // Wrap the form in a JScrollPane to handle overflow
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(backgroundColor);
        
        // Create button panel with proper spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(backgroundColor);
        
        // Register button with gradient
        registerButton = new JButton("Register") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient for button
                GradientPaint gp = new GradientPaint(
                    0, 0, primaryColor,
                    0, getHeight(), new Color(70, 60, 200));
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                
                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(boldFont);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        registerButton.setOpaque(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setFont(boldFont);
        registerButton.setForeground(Color.WHITE);
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(boldFont);
        cancelButton.setForeground(primaryColor);
        cancelButton.setBackground(backgroundColor);
        cancelButton.setBorder(BorderFactory.createLineBorder(primaryColor, 1, true));
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        // Note about required fields
        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        notePanel.setBackground(backgroundColor);
        
        JLabel requiredNote = new JLabel("* Required fields");
        requiredNote.setFont(new Font("Arial", Font.ITALIC, 12));
        requiredNote.setForeground(new Color(100, 100, 100));
        
        notePanel.add(requiredNote);
        
        // Add components to main panel with proper layout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(backgroundColor);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(notePanel, BorderLayout.SOUTH);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        registerButton.addActionListener(e -> handleRegister());
        cancelButton.addActionListener(e -> dispose());
        
        // Set default button
        getRootPane().setDefaultButton(registerButton);
        
        // Add main panel to dialog
        setContentPane(mainPanel);
        
        // Pack the dialog to fit its contents
        pack();
        
        // Ensure the dialog isn't too small
        Dimension size = getSize();
        setSize(Math.max(size.width, 450), Math.max(size.height, 550));
    }
    
    private void handleRegister() {
        // Get form values
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        
        // Validate required fields
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all required fields.",
                "Missing Information",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate password match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match.",
                "Password Error",
                JOptionPane.WARNING_MESSAGE);
            passwordField.setText("");
            confirmPasswordField.setText("");
            passwordField.requestFocus();
            return;
        }
        
        // Validate password strength
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this,
                "Password must be at least 6 characters long.",
                "Weak Password",
                JOptionPane.WARNING_MESSAGE);
            passwordField.setText("");
            confirmPasswordField.setText("");
            passwordField.requestFocus();
            return;
        }
        
        // Validate email format
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid email address.",
                "Invalid Email",
                JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        // Check if username already exists
        Optional<User> existingUser = userDAO.findByUsername(username);
        if (existingUser.isPresent()) {
            JOptionPane.showMessageDialog(this,
                "Username already exists. Please choose another one.",
                "Username Unavailable",
                JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }
        
        // Change cursor to wait cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        registerButton.setEnabled(false);
        registerButton.setText("Creating...");
        
        // Create and save new user
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(password); // UserDAO will handle hashing
                    user.setEmail(email);
                    user.setFullName(fullName);
                    user.setAdmin(false); // Default to regular user
                    
                    userDAO.create(user);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    setCursor(Cursor.getDefaultCursor());
                    registerButton.setEnabled(true);
                    registerButton.setText("Register");
                    
                    if (get()) {
                        JOptionPane.showMessageDialog(RegisterDialog.this,
                            "Registration successful! You can now log in.",
                            "Account Created",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Close the dialog
                    } else {
                        JOptionPane.showMessageDialog(RegisterDialog.this,
                            "Error creating user. Please try again.",
                            "Registration Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RegisterDialog.this,
                        "Error: " + e.getMessage(),
                        "System Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
} 