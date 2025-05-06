package com.quizapp.view.main;

import com.quizapp.controller.AuthController;
import com.quizapp.controller.QuizController;
import com.quizapp.model.Category;
import com.quizapp.model.QuizResult;
import com.quizapp.service.ApplicationEventManager;
import com.quizapp.service.ApplicationEventManager.EventType;
import com.quizapp.service.ApplicationEventManager.EventListener;
import com.quizapp.service.AuthService;
import com.quizapp.service.SessionService;
import com.quizapp.util.AccessibilityManager;
import com.quizapp.util.KeyboardShortcutManager;
import com.quizapp.util.ThemeManager;
import com.quizapp.view.admin.AdminPanel;
import com.quizapp.view.auth.LoginFrame;
import com.quizapp.view.common.Toast;
import com.quizapp.view.profile.ProfileDialog;
import com.quizapp.view.quiz.CategorySelectionPanel;
import com.quizapp.view.quiz.CategorySelectionPanel.QuizStartListener;
import com.quizapp.view.quiz.QuizPanel;
import com.quizapp.view.scoring.LeaderboardPanel;
import com.quizapp.view.settings.SettingsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * The main application window displayed after successful login.
 * Uses CardLayout to switch between different panels.
 */
public class MainFrame extends JFrame implements QuizStartListener, EventListener {

    private JMenuBar menuBar;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private CategorySelectionPanel categorySelectionPanel;
    private QuizPanel quizPanel;
    private AdminPanel adminPanel;
    private LeaderboardPanel leaderboardPanel;
    private final int userId;
    private final boolean isAdmin;
    private JPanel statusPanel;
    private JLabel userRoleLabel;
    private JLabel userStatusLabel;
    private String username;
    private boolean mobileMode = false;
    private String currentPanel;

    // Panel names for CardLayout
    private static final String CATEGORY_SELECTION_PANEL = "categorySelection";
    private static final String QUIZ_PANEL = "quiz";
    private static final String SCORE_PANEL = "score";
    private static final String LEADERBOARD_PANEL = "leaderboard";
    private static final String ADMIN_PANEL = "admin";

    // Colors for UI differences
    private static final Color ADMIN_COLOR = new Color(230, 240, 255); // Light blue for admin
    private static final Color USER_COLOR = new Color(240, 255, 240);  // Light green for users

    public MainFrame(int userId, boolean isAdmin, String username) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.username = username;
        
        setTitle("Quiz Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        
        // Set background color based on user role
        Container contentPane = getContentPane();
        contentPane.setBackground(isAdmin ? ADMIN_COLOR : USER_COLOR);

        // Initialize components
        initializeMenuBar();
        initializeStatusPanel();
        initializeMainContent();

        // Create main container with status panel on top
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.add(statusPanel, BorderLayout.NORTH);
        mainContainer.add(mainContentPanel, BorderLayout.CENTER);

        // Add components to frame
        setJMenuBar(menuBar);
        add(mainContainer);

        // Set up session timeout listener
        SessionService.getInstance().setSessionTimeoutListener(this::handleSessionTimeout);

        // Register for application events
        ApplicationEventManager.getInstance().addListener(EventType.CATEGORY_UPDATED, this);

        // Show category selection panel by default
        showCategorySelectionPanel();
        
        // Apply theme
        ThemeManager.applyTheme();
        
        // Apply accessibility settings
        AccessibilityManager.applyAccessibilitySettings(this);
        
        // Register keyboard shortcuts
        registerKeyboardShortcuts();
        
        // Add mouse and keyboard listeners to track user activity
        addActivityTracking();
        
        // Show welcome toast
        SwingUtilities.invokeLater(() -> {
            Toast.success(this, "Welcome, " + username + "!");
        });
    }
    
    /**
     * Initializes the application menu bar
     */
    private void initializeMenuBar() {
        menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem newQuizItem = new JMenuItem("New Quiz");
        newQuizItem.addActionListener(e -> showCategorySelectionPanel());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(newQuizItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        
        JMenuItem leaderboardItem = new JMenuItem("Leaderboard");
        leaderboardItem.addActionListener(e -> showLeaderboardPanel());
        
        JMenuItem profileItem = new JMenuItem("Profile");
        profileItem.addActionListener(e -> showProfileDialog());
        
        JCheckBoxMenuItem mobileItem = new JCheckBoxMenuItem("Mobile Mode");
        mobileItem.addActionListener(e -> setMobileMode(mobileItem.isSelected()));
        
        viewMenu.add(leaderboardItem);
        viewMenu.add(profileItem);
        viewMenu.addSeparator();
        viewMenu.add(mobileItem);
        
        // Admin menu (only visible to admins)
        JMenu adminMenu = new JMenu("Admin");
        adminMenu.setMnemonic(KeyEvent.VK_A);
        
        JMenuItem adminPanelItem = new JMenuItem("Admin Panel");
        adminPanelItem.addActionListener(e -> showAdminPanel());
        
        adminMenu.add(adminPanelItem);
        
        // Options menu
        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O);
        
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> new SettingsDialog(this).setVisible(true));
        
        JMenuItem themeItem = new JMenuItem("Toggle Theme");
        themeItem.addActionListener(e -> ThemeManager.toggleTheme(this));
        
        optionsMenu.add(settingsItem);
        optionsMenu.add(themeItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem shortcutsItem = new JMenuItem("Keyboard Shortcuts");
        shortcutsItem.addActionListener(e -> showKeyboardShortcutsDialog());
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Quiz Application Version 1.0\n© 2023 Java Quiz App Team",
                "About Quiz Application",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        helpMenu.add(shortcutsItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        if (isAdmin) {
            menuBar.add(adminMenu);
        }
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);
        
        // Add logout button on the right side
        menuBar.add(Box.createHorizontalGlue());
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());
        menuBar.add(logoutButton);
    }
    
    private void initializeStatusPanel() {
        statusPanel = new JPanel(new BorderLayout(10, 0));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // User info panel
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        userRoleLabel = new JLabel(isAdmin ? "Administrator" : "User");
        userRoleLabel.setFont(userRoleLabel.getFont().deriveFont(Font.BOLD, 13f));
        userRoleLabel.setForeground(isAdmin ? new Color(41, 128, 185) : new Color(46, 204, 113));
        
        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(usernameLabel.getFont().deriveFont(Font.PLAIN, 13f));
        
        userInfoPanel.add(userRoleLabel);
        userInfoPanel.add(new JLabel("•"));
        userInfoPanel.add(usernameLabel);
        
        // Status info panel
        JPanel statusInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userStatusLabel = new JLabel("Session Active");
        userStatusLabel.setFont(userStatusLabel.getFont().deriveFont(Font.PLAIN, 13f));
        userStatusLabel.setForeground(new Color(46, 204, 113));
        
        // Add session timer
        JLabel timerLabel = new JLabel("00:00");
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.PLAIN, 13f));
        timerLabel.setForeground(new Color(149, 165, 166));
        
        statusInfoPanel.add(timerLabel);
        statusInfoPanel.add(new JLabel("•"));
        statusInfoPanel.add(userStatusLabel);
        
        statusPanel.add(userInfoPanel, BorderLayout.WEST);
        statusPanel.add(statusInfoPanel, BorderLayout.EAST);
        
        // Start session timer
        startSessionTimer(timerLabel);
    }
    
    private void startSessionTimer(JLabel timerLabel) {
        Timer timer = new Timer(1000, e -> {
            // Just show a simple timer counting up from login
            long elapsedTime = System.currentTimeMillis() - System.currentTimeMillis() % 1000;
            long seconds = (elapsedTime / 1000) % 60;
            long minutes = (elapsedTime / (1000 * 60)) % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        });
        timer.start();
    }

    private void initializeMainContent() {
        mainContentPanel = new JPanel(new CardLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize panels
        categorySelectionPanel = new CategorySelectionPanel(this);
        quizPanel = new QuizPanel(userId);
        adminPanel = new AdminPanel();
        leaderboardPanel = new LeaderboardPanel();

        // Add panels to card layout
        mainContentPanel.add(categorySelectionPanel, CATEGORY_SELECTION_PANEL);
        mainContentPanel.add(quizPanel, QUIZ_PANEL);
        mainContentPanel.add(adminPanel, ADMIN_PANEL);
        mainContentPanel.add(leaderboardPanel, LEADERBOARD_PANEL);
        
        cardLayout = (CardLayout) mainContentPanel.getLayout();
    }

    @Override
    public void onQuizStart(Category selectedCategory) {
        startQuiz(selectedCategory);
    }

    private void showCategorySelectionPanel() {
        if (CATEGORY_SELECTION_PANEL.equals(currentPanel)) {
            return; // Already showing this panel
        }
        
        // Check if we need to refresh categories (could have been updated in the admin panel)
        categorySelectionPanel.refreshCategories();
        
        // Directly show the panel without animations
        cardLayout.show(mainContentPanel, CATEGORY_SELECTION_PANEL);
        currentPanel = CATEGORY_SELECTION_PANEL;
        setTitle("Quiz Application - Select Category");
    }

    private void showAdminPanel() {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, 
                "You do not have permission to access the admin panel.",
                "Access Denied",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (ADMIN_PANEL.equals(currentPanel)) {
            return; // Already showing this panel
        }
        
        // Directly show the panel without animations
        cardLayout.show(mainContentPanel, ADMIN_PANEL);
        currentPanel = ADMIN_PANEL;
        setTitle("Quiz Application - Admin Panel");
    }

    private void showLeaderboardPanel() {
        // Refresh the leaderboard data before showing it
        leaderboardPanel.refreshData();
        
        if (LEADERBOARD_PANEL.equals(currentPanel)) {
            return; // Already showing this panel
        }
        
        // Show simple loading message
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Use SwingWorker to avoid freezing the UI
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Background operations if needed
                try {
                    Thread.sleep(500); // Brief pause to load data
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }
            
            @Override
            protected void done() {
                // Show the panel directly
                cardLayout.show(mainContentPanel, LEADERBOARD_PANEL);
                currentPanel = LEADERBOARD_PANEL;
                setTitle("Quiz Application - Leaderboard");
                setCursor(Cursor.getDefaultCursor());
            }
        };
        worker.execute();
    }

    private void showProfileDialog() {
        new ProfileDialog(this, userId).setVisible(true);
    }
    
    private void showKeyboardShortcutsDialog() {
        JDialog shortcutsDialog = new JDialog(this, "Keyboard Shortcuts", true);
        shortcutsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create shortcuts table
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
        
        JScrollPane scrollPane = new JScrollPane(shortcutsTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> shortcutsDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        shortcutsDialog.setContentPane(contentPanel);
        shortcutsDialog.setSize(400, 400);
        shortcutsDialog.setLocationRelativeTo(this);
        shortcutsDialog.setVisible(true);
    }

    private Component getPanelByName(String name) {
        for (Component component : mainContentPanel.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
        }
        return null;
    }

    private void startQuiz(Category selectedCategory) {
        // Default settings: use all available questions and 120 seconds (2 minutes) timeout
        int questionCount = 5; // Use 5 questions or all if there are fewer
        int quizTimeSeconds = 120; // 2 minutes
        
        // Show a confirmation dialog before starting the quiz
        String message = String.format(
            "You're about to start a quiz on '%s'.\n\n" +
            "• Number of questions: %d\n" +
            "• Time limit: %d minutes\n\n" +
            "Are you ready to begin?",
            selectedCategory.getName(),
            questionCount,
            quizTimeSeconds / 60
        );
        
        int response = JOptionPane.showConfirmDialog(
            this,
            message,
            "Start Quiz",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (response == JOptionPane.YES_OPTION) {
            // Start the quiz with the selected category
            quizPanel.startQuiz(selectedCategory, questionCount, quizTimeSeconds, this::showCategorySelectionPanel);
            
            // Show brief loading indication
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // Use SwingWorker to avoid freezing the UI
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        Thread.sleep(500); // Brief pause to prepare quiz
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    // Directly show quiz panel
        cardLayout.show(mainContentPanel, QUIZ_PANEL);
                    currentPanel = QUIZ_PANEL;
                    setTitle("Quiz Application - " + selectedCategory.getName() + " Quiz");
                    setCursor(Cursor.getDefaultCursor());
                }
            };
            worker.execute();
        }
    }
    
    private void registerKeyboardShortcuts() {
        // Register shortcut for toggling theme
        KeyboardShortcutManager.registerShortcut(
            (JComponent)getContentPane(), 
            "toggleTheme", 
            () -> ThemeManager.toggleTheme(this)
        );
        
        // Register shortcut for viewing profile
        KeyboardShortcutManager.registerShortcut(
            (JComponent)getContentPane(), 
            "viewProfile", 
            this::showProfileDialog
        );
        
        // Register shortcut for new quiz
        KeyboardShortcutManager.registerShortcut(
            (JComponent)getContentPane(), 
            "newQuiz", 
            this::showCategorySelectionPanel
        );
        
        // Register shortcut for leaderboard
        KeyboardShortcutManager.registerShortcut(
            (JComponent)getContentPane(), 
            "leaderboard", 
            this::showLeaderboardPanel
        );
        
        // Register shortcut for admin panel (if admin)
        if (isAdmin) {
            KeyboardShortcutManager.registerShortcut(
                (JComponent)getContentPane(), 
                "adminPanel", 
                this::showAdminPanel
            );
        }
        
        // Register shortcut for logout
        KeyboardShortcutManager.registerShortcut(
            (JComponent)getContentPane(), 
            "logout", 
            this::logout
        );
        
        // Register shortcut for help
        KeyboardShortcutManager.registerShortcut(
            (JComponent)getContentPane(), 
            "help", 
            this::showKeyboardShortcutsDialog
        );
    }

    private void handleSessionTimeout() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "Your session has expired due to inactivity. Please log in again.",
                "Session Expired",
                JOptionPane.WARNING_MESSAGE);
            logout();
        });
    }

    // Add mouse and keyboard listeners to track user activity
    private void addActivityTracking() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                SessionService.getInstance().updateLastActivity();
            }
        };

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SessionService.getInstance().updateLastActivity();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addKeyListener(keyAdapter);
        setFocusable(true);
    }

    /**
     * Logs out the current user and returns to the login screen
     */
    private void logout() {
        // Clean up resources
        if (categorySelectionPanel != null) {
            categorySelectionPanel.cleanup();
        }
        
        // Properly log out through SessionService to clear tokens and session data
        try {
            SessionService.getInstance().logout();
            System.out.println("User logged out successfully: " + username);
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Proceed with logout
        JOptionPane.showMessageDialog(
            this,
            "You have been logged out successfully.",
            "Logout",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Close this frame
        dispose();
        
        // Show login screen
        new LoginFrame().setVisible(true);
    }

    private void setMobileMode(boolean enabled) {
        this.mobileMode = enabled;
        if (quizPanel != null) quizPanel.setMobileMode(enabled);
        if (categorySelectionPanel != null) categorySelectionPanel.setMobileMode(enabled);
        // Optionally update other panels
        SwingUtilities.updateComponentTreeUI(this);
        
        // Show confirmation toast
        String message = enabled ? "Mobile mode enabled" : "Mobile mode disabled";
        Toast.info(this, message);
    }

    public boolean isMobileMode() {
        return mobileMode;
    }

    /**
     * Shows a simple status message in a non-blocking way
     * 
     * @param message The message to display
     */
    private void showStatus(String message) {
        // Update status panel to show message temporarily
        JLabel statusLabel = new JLabel(message);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        
        // Store original components
        Component[] originalComponents = statusPanel.getComponents();
        Object[] originalConstraints = new Object[originalComponents.length];
        
        // Identify constraints
        BorderLayout layout = (BorderLayout) statusPanel.getLayout();
        for (int i = 0; i < originalComponents.length; i++) {
            if (originalComponents[i] == layout.getLayoutComponent(BorderLayout.WEST)) {
                originalConstraints[i] = BorderLayout.WEST;
            } else if (originalComponents[i] == layout.getLayoutComponent(BorderLayout.EAST)) {
                originalConstraints[i] = BorderLayout.EAST;
            } else if (originalComponents[i] == layout.getLayoutComponent(BorderLayout.NORTH)) {
                originalConstraints[i] = BorderLayout.NORTH;
            } else if (originalComponents[i] == layout.getLayoutComponent(BorderLayout.SOUTH)) {
                originalConstraints[i] = BorderLayout.SOUTH;
            } else if (originalComponents[i] == layout.getLayoutComponent(BorderLayout.CENTER)) {
                originalConstraints[i] = BorderLayout.CENTER;
            }
        }
        
        // Replace with status message
        statusPanel.removeAll();
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.revalidate();
        statusPanel.repaint();
        
        // Restore after delay
        Timer timer = new Timer(1500, e -> {
            statusPanel.removeAll();
            for (int i = 0; i < originalComponents.length; i++) {
                if (originalConstraints[i] != null) {
                    statusPanel.add(originalComponents[i], originalConstraints[i]);
                }
            }
            statusPanel.revalidate();
            statusPanel.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Shows a simple loading message at the bottom of the screen
     * 
     * @param message The message to display
     * @return The status label that can be updated
     */
    private JLabel showLoadingStatus(String message) {
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(new Color(240, 240, 240));
        loadingPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JLabel statusLabel = new JLabel(message);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        loadingPanel.add(statusLabel, BorderLayout.CENTER);
        
        Container contentPane = getContentPane();
        BorderLayout layout = (BorderLayout) contentPane.getLayout();
        Component oldSouth = layout.getLayoutComponent(BorderLayout.SOUTH);
        
        contentPane.add(loadingPanel, BorderLayout.SOUTH);
        contentPane.revalidate();
        contentPane.repaint();
        
        return statusLabel;
    }

    /**
     * Handle application events from the EventManager
     */
    @Override
    public void onEvent(EventType eventType, Object data) {
        if (eventType == EventType.CATEGORY_UPDATED) {
            // Refresh categories in the selection panel
            SwingUtilities.invokeLater(() -> {
                // If we're showing the category selection panel, refresh it
                if (categorySelectionPanel != null && CATEGORY_SELECTION_PANEL.equals(currentPanel)) {
                    categorySelectionPanel.refreshCategories();
                }
                
                // Also refresh leaderboard if it's visible
                if (leaderboardPanel != null && LEADERBOARD_PANEL.equals(currentPanel)) {
                    leaderboardPanel.refreshData();
                }
            });
        }
    }

    // When the app is closing, clean up resources
    @Override
    public void dispose() {
        // Unregister from event manager
        ApplicationEventManager.getInstance().removeListener(EventType.CATEGORY_UPDATED, this);
        
        // Clean up event listeners
        if (categorySelectionPanel != null) {
            categorySelectionPanel.cleanup();
        }
        
        // Call parent dispose
        super.dispose();
    }

    // Example main method for testing MainFrame directly (REMOVE LATER)
    // public static void main(String[] args) {
    //     // Use FlatLaf if added to pom.xml
    //     try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); } catch (Exception ex) {}
    //     // Simulate launching as an admin user
    //     SwingUtilities.invokeLater(() -> new MainFrame(true));
    // }
}