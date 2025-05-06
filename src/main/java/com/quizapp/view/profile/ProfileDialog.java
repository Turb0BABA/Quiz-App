package com.quizapp.view.profile;

import com.quizapp.controller.ProfileController;
import com.quizapp.model.QuizResult;
import com.quizapp.model.User;
import com.quizapp.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class ProfileDialog extends JDialog {
    private final ProfileController profileController;
    private JTextField usernameField;
    private JTextField emailField;
    private JButton saveButton;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTabbedPane tabbedPane;
    private JPanel statsPanel;
    private JPanel achievementsPanel;
    private Map<String, Integer> categoryScores;
    private User user;
    private List<QuizResult> quizHistory;

    public ProfileDialog(JFrame parent, int userId) {
        super(parent, "User Profile", true);
        this.profileController = new ProfileController(userId);
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Create and set main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        setContentPane(mainPanel);
        
        // Create header panel with user info
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane for different sections
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font(tabbedPane.getFont().getName(), Font.BOLD, 14));
        
        // Add tabs with content
        tabbedPane.addTab("Quiz History", createHistoryPanel());
        tabbedPane.addTab("Statistics", createStatsPanel());
        tabbedPane.addTab("Achievements", createAchievementsPanel());
        tabbedPane.addTab("Settings", createSettingsPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Create footer with save button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        
        saveButton = UIConstants.createPrimaryButton("Save Changes");
        saveButton.addActionListener(e -> saveProfile());
        
        JButton closeButton = UIConstants.createSecondaryButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        footerPanel.add(saveButton);
        footerPanel.add(closeButton);
        
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Load user data
        loadProfile();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(0, 0, 15, 0)
        ));
        
        // Create avatar panel
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw avatar circle
                int size = Math.min(getWidth(), getHeight()) - 10;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                g2d.setColor(UIConstants.PRIMARY_COLOR);
                g2d.fillOval(x, y, size, size);
                
                // Get first letter of username (will be updated after loading)
                String initial = "U";
                
                // Draw user initial
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(getFont().getName(), Font.BOLD, 36));
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(initial);
                int textHeight = fm.getHeight();
                
                g2d.drawString(initial, 
                    x + (size - textWidth) / 2, 
                    y + (size - textHeight) / 2 + fm.getAscent());
            }
        };
        avatarPanel.setPreferredSize(new Dimension(100, 100));
        avatarPanel.setOpaque(false);
        
        // Create info panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        infoPanel.setOpaque(false);
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font(usernameLabel.getFont().getName(), Font.BOLD, 14));
        
        usernameField = new JTextField();
        usernameField.setFont(new Font(usernameField.getFont().getName(), Font.PLAIN, 14));
        
        // Email field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font(emailLabel.getFont().getName(), Font.BOLD, 14));
        
        emailField = new JTextField();
        emailField.setFont(new Font(emailField.getFont().getName(), Font.PLAIN, 14));
        
        // Add components to info panel
        infoPanel.add(usernameLabel);
        infoPanel.add(usernameField);
        infoPanel.add(emailLabel);
        infoPanel.add(emailField);
        
        // Assemble header panel
        panel.add(avatarPanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 5, 5, 5));
        
        // Create table model with improved columns
        tableModel = new DefaultTableModel(
            new Object[]{"Category", "Score", "Percentage", "Questions", "Time Taken", "Date"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1) return Integer.class; // Score
                if (column == 2) return Double.class;  // Percentage
                if (column == 3) return Integer.class; // Questions
                if (column == 4) return Integer.class; // Time Taken
                return Object.class;
            }
        };
        
        // Create table with modern styling
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(35);
        historyTable.setShowGrid(false);
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Custom header renderer
        JTableHeader header = historyTable.getTableHeader();
        header.setFont(new Font(header.getFont().getName(), Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBackground(UIConstants.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        
        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Category
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Score
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Percentage
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Questions
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Time Taken
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Date
        
        // Custom cell renderer
        historyTable.setDefaultRenderer(Object.class, new HistoryCellRenderer());
        historyTable.setDefaultRenderer(Integer.class, new HistoryCellRenderer());
        historyTable.setDefaultRenderer(Double.class, new HistoryCellRenderer());
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Custom cell renderer for quiz history table
     */
    private class HistoryCellRenderer extends DefaultTableCellRenderer {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Set alignment
            if (column == 0) {
                setHorizontalAlignment(LEFT);
            } else {
                setHorizontalAlignment(CENTER);
            }
            
            // Apply alternating row colors
            if (!isSelected) {
                if (row % 2 == 0) {
                    setBackground(UIConstants.CARD_BACKGROUND);
                } else {
                    setBackground(UIConstants.CARD_HOVER_BACKGROUND);
                }
                setForeground(UIConstants.TEXT_COLOR);
            }
            
            // Format percentages
            if (column == 2 && value != null) {
                Double percentage = (Double) value;
                setText(String.format("%.1f%%", percentage));
                
                // Color based on percentage
                if (percentage >= 90) {
                    setForeground(UIConstants.SUCCESS_COLOR);
                } else if (percentage >= 70) {
                    setForeground(UIConstants.SECONDARY_COLOR);
                } else if (percentage >= 50) {
                    setForeground(UIConstants.WARNING_COLOR);
                } else {
                    setForeground(UIConstants.ERROR_COLOR);
                }
            }
            
            // Format time
            if (column == 4 && value != null) {
                int seconds = (int) value;
                if (seconds < 60) {
                    setText(seconds + " sec");
                } else {
                    setText(seconds / 60 + "m " + seconds % 60 + "s");
                }
            }
            
            // Format dates
            if (column == 5 && value instanceof Date) {
                setText(dateFormat.format((Date) value));
            }
            
            setBorder(new EmptyBorder(2, 10, 2, 10));
            return c;
        }
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 5, 5, 5));
        
        // Create container for stats cards and charts
        JPanel statsContainer = new JPanel(new BorderLayout(20, 20));
        statsContainer.setOpaque(false);
        
        // Stats cards
        JPanel statsCardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsCardsPanel.setOpaque(false);
        
        // These will be populated when data is loaded
        statsCardsPanel.add(createStatCard("Total Quizzes", "0", UIConstants.PRIMARY_COLOR));
        statsCardsPanel.add(createStatCard("Avg. Score", "0%", UIConstants.SECONDARY_COLOR));
        statsCardsPanel.add(createStatCard("Best Score", "0%", UIConstants.SUCCESS_COLOR));
        statsCardsPanel.add(createStatCard("Total Time", "0 min", UIConstants.ACCENT_COLOR));
        
        // Charts panel
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setOpaque(false);
        
        // Category performance panel (will be populated with data)
        JPanel categoryPanel = new JPanel(new BorderLayout(0, 10));
        categoryPanel.setOpaque(false);
        categoryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel categoryTitle = new JLabel("Performance by Category", SwingConstants.CENTER);
        categoryTitle.setFont(new Font(categoryTitle.getFont().getName(), Font.BOLD, 14));
        categoryPanel.add(categoryTitle, BorderLayout.NORTH);
        
        // Add placeholder for category chart
        JPanel categoryChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Chart will be drawn here when data is loaded
                g.setColor(new Color(240, 240, 240));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.GRAY);
                g.drawString("Category data will be shown here", 50, getHeight() / 2);
            }
        };
        categoryChartPanel.setOpaque(false);
        categoryPanel.add(categoryChartPanel, BorderLayout.CENTER);
        
        // Progress over time panel (will be populated with data)
        JPanel progressPanel = new JPanel(new BorderLayout(0, 10));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel progressTitle = new JLabel("Progress Over Time", SwingConstants.CENTER);
        progressTitle.setFont(new Font(progressTitle.getFont().getName(), Font.BOLD, 14));
        progressPanel.add(progressTitle, BorderLayout.NORTH);
        
        // Add placeholder for progress chart
        JPanel progressChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Chart will be drawn here when data is loaded
                g.setColor(new Color(240, 240, 240));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.GRAY);
                g.drawString("Progress data will be shown here", 50, getHeight() / 2);
            }
        };
        progressChartPanel.setOpaque(false);
        progressPanel.add(progressChartPanel, BorderLayout.CENTER);
        
        chartsPanel.add(categoryPanel);
        chartsPanel.add(progressPanel);
        
        // Assemble stats container
        statsContainer.add(statsCardsPanel, BorderLayout.NORTH);
        statsContainer.add(chartsPanel, BorderLayout.CENTER);
        
        // Save reference for updating later
        statsPanel = statsContainer;
        panel.add(statsContainer, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font(valueLabel.getFont().getName(), Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(titleLabel);
        
        return panel;
    }
    
    private JPanel createAchievementsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 5, 5, 5));
        
        // Create achievements grid
        JPanel achievementsGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        achievementsGrid.setOpaque(false);
        
        // Add sample achievements (these will be updated based on user data)
        achievementsGrid.add(createAchievementCard("First Quiz", "Complete your first quiz", false));
        achievementsGrid.add(createAchievementCard("Perfect Score", "Get 100% on any quiz", false));
        achievementsGrid.add(createAchievementCard("Quiz Master", "Complete 10 quizzes", false));
        achievementsGrid.add(createAchievementCard("Speed Demon", "Complete a quiz in under 1 minute", false));
        achievementsGrid.add(createAchievementCard("All-Rounder", "Complete quizzes in 5 different categories", false));
        achievementsGrid.add(createAchievementCard("Persistence", "Retry a failed quiz and pass it", false));
        
        // Save reference for updating later
        achievementsPanel = achievementsGrid;
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(achievementsGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAchievementCard(String title, String description, boolean unlocked) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(unlocked ? new Color(220, 220, 220) : new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(unlocked ? UIConstants.CARD_BACKGROUND : new Color(245, 245, 245));
        
        // Create icon panel
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circle
                int size = Math.min(getWidth(), getHeight()) - 10;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                if (unlocked) {
                    g2d.setColor(UIConstants.SUCCESS_COLOR);
                    g2d.fillOval(x, y, size, size);
                    
                    // Draw checkmark or trophy
                    g2d.setColor(Color.WHITE);
                    int[] xPoints = {x + size/4, x + size/2, x + size*3/4};
                    int[] yPoints = {y + size/2, y + size*3/4, y + size/4};
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawPolyline(xPoints, yPoints, 3);
                } else {
                    g2d.setColor(new Color(180, 180, 180));
                    g2d.fillOval(x, y, size, size);
                    
                    // Draw lock
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    int lockWidth = size / 2;
                    int lockHeight = size / 2;
                    int lockX = x + (size - lockWidth) / 2;
                    int lockY = y + (size - lockHeight) / 2;
                    g2d.fillRect(lockX, lockY, lockWidth, lockHeight);
                    g2d.drawRect(lockX, lockY, lockWidth, lockHeight);
                    
                    // Draw lock hole
                    g2d.fillOval(lockX + lockWidth/3, lockY + lockHeight/3, lockWidth/3, lockHeight/3);
                }
            }
        };
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setOpaque(false);
        
        // Create content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));
        titleLabel.setForeground(unlocked ? UIConstants.TEXT_COLOR : new Color(150, 150, 150));
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font(descLabel.getFont().getName(), Font.PLAIN, 12));
        descLabel.setForeground(unlocked ? new Color(100, 100, 100) : new Color(150, 150, 150));
        
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(descLabel);
        
        // Assemble panel
        panel.add(iconPanel, BorderLayout.WEST);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 5, 5, 5));
        
        // Create settings form
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        formPanel.setOpaque(false);
        
        // Notification settings
        JLabel notificationsLabel = new JLabel("Email Notifications:");
        notificationsLabel.setFont(new Font(notificationsLabel.getFont().getName(), Font.BOLD, 14));
        
        JCheckBox notificationsCheckbox = new JCheckBox("Receive email notifications");
        notificationsCheckbox.setOpaque(false);
        
        // Privacy settings
        JLabel privacyLabel = new JLabel("Privacy:");
        privacyLabel.setFont(new Font(privacyLabel.getFont().getName(), Font.BOLD, 14));
        
        JCheckBox privacyCheckbox = new JCheckBox("Show my scores on leaderboards");
        privacyCheckbox.setOpaque(false);
        privacyCheckbox.setSelected(true);
        
        // Add components
        formPanel.add(notificationsLabel);
        formPanel.add(notificationsCheckbox);
        formPanel.add(privacyLabel);
        formPanel.add(privacyCheckbox);
        
        // Add form to panel
        panel.add(formPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private void loadProfile() {
        // Get user data
        user = profileController.getUser();
        if (user != null) {
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            
            // Update avatar initial
            JPanel headerPanel = (JPanel) getContentPane().getComponent(0);
            JPanel avatarPanel = (JPanel) headerPanel.getComponent(0);
            avatarPanel.repaint(); // This will redraw with the correct initial
        }
        
        // Get quiz history
        quizHistory = profileController.getQuizHistory();
        
        // Process history data
        categoryScores = new HashMap<>();
        int totalQuizzes = quizHistory.size();
        int totalScore = 0;
        int bestScore = 0;
        double bestPercentage = 0;
        int totalTimeSeconds = 0;
        
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Load history table and calculate statistics
        for (QuizResult result : quizHistory) {
            // Calculate percentage
            double percentage = 0;
            if (result.getTotalQuestions() > 0) {
                percentage = (double) result.getScore() / result.getTotalQuestions() * 100;
            }
            
            // Calculate time taken (demo value)
            int timeSeconds = 30 + (int)(Math.random() * 120); // Random time between 30s and 150s
            
            // Add row to table
            tableModel.addRow(new Object[]{
                result.getCategoryName(),
                result.getScore(),
                percentage,
                result.getTotalQuestions(),
                timeSeconds,
                result.getCompletedAt()
            });
            
            // Update statistics
            totalScore += result.getScore();
            totalTimeSeconds += timeSeconds;
            
            if (percentage > bestPercentage) {
                bestPercentage = percentage;
                bestScore = result.getScore();
            }
            
            // Track category scores
            String category = result.getCategoryName();
            categoryScores.put(category, categoryScores.getOrDefault(category, 0) + result.getScore());
        }
        
        // Update stats cards
        if (totalQuizzes > 0) {
            updateStatsCards(totalQuizzes, totalScore, bestPercentage, totalTimeSeconds);
        }
        
        // Update achievements
        updateAchievements(totalQuizzes, bestPercentage, categoryScores.size());
    }
    
    private void updateStatsCards(int totalQuizzes, int totalScore, double bestPercentage, int totalTimeSeconds) {
        // Calculate average score
        double avgPercentage = 0;
        if (totalQuizzes > 0) {
            avgPercentage = (double) totalScore / (totalQuizzes * 100) * 100;
        }
        
        // Format time
        String timeString;
        if (totalTimeSeconds < 60) {
            timeString = totalTimeSeconds + " sec";
        } else if (totalTimeSeconds < 3600) {
            timeString = (totalTimeSeconds / 60) + " min";
        } else {
            timeString = (totalTimeSeconds / 3600) + "h " + ((totalTimeSeconds % 3600) / 60) + "m";
        }
        
        // Get stats panel (first tab)
        Component statsTab = tabbedPane.getComponentAt(1);
        if (!(statsTab instanceof Container)) return;
        
        Container statsContainer = (Container) statsTab;
        if (statsContainer.getComponentCount() == 0) return;
        
        Component statsCardsComponent = statsContainer.getComponent(0);
        if (!(statsCardsComponent instanceof Container)) return;
        
        Container statsCardsPanel = (Container) statsCardsComponent;
        if (statsCardsPanel.getComponentCount() < 4) return;
        
        // Update total quizzes card
        updateStatCardValue(statsCardsPanel.getComponent(0), String.valueOf(totalQuizzes));
        
        // Update average score card
        updateStatCardValue(statsCardsPanel.getComponent(1), String.format("%.1f%%", avgPercentage));
        
        // Update best score card
        updateStatCardValue(statsCardsPanel.getComponent(2), String.format("%.1f%%", bestPercentage));
        
        // Update time card
        updateStatCardValue(statsCardsPanel.getComponent(3), timeString);
    }
    
    private void updateStatCardValue(Component statCard, String value) {
        if (!(statCard instanceof Container)) return;
        
        Container panel = (Container) statCard;
        if (panel.getComponentCount() == 0) return;
        
        // Find the value label (should be first component)
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setText(value);
                break;
            }
        }
    }
    
    private void updateAchievements(int totalQuizzes, double bestPercentage, int categoryCount) {
        // Determine which achievements are unlocked
        boolean firstQuizUnlocked = totalQuizzes >= 1;
        boolean perfectScoreUnlocked = bestPercentage >= 99.9;
        boolean quizMasterUnlocked = totalQuizzes >= 10;
        boolean speedDemonUnlocked = false; // We don't have real time data
        boolean allRounderUnlocked = categoryCount >= 5;
        boolean persistenceUnlocked = false; // We don't have this data
        
        // Get achievements panel (second tab)
        Component achievementsTabContent = tabbedPane.getComponentAt(2);
        if (achievementsTabContent != null && achievementsTabContent instanceof Container) {
            Container container = (Container) achievementsTabContent;
            if (container.getComponentCount() > 0) {
                Component scrollPane = container.getComponent(0);
                if (scrollPane instanceof JScrollPane) {
                    JViewport viewport = ((JScrollPane) scrollPane).getViewport();
                    if (viewport != null && viewport.getView() instanceof JPanel) {
                        JPanel achievementsGrid = (JPanel) viewport.getView();
                
                        // Update achievement cards
                        updateAchievementCard(achievementsGrid, 0, "First Quiz", "Complete your first quiz", firstQuizUnlocked);
                        updateAchievementCard(achievementsGrid, 1, "Perfect Score", "Get 100% on any quiz", perfectScoreUnlocked);
                        updateAchievementCard(achievementsGrid, 2, "Quiz Master", "Complete 10 quizzes", quizMasterUnlocked);
                        updateAchievementCard(achievementsGrid, 3, "Speed Demon", "Complete a quiz in under 1 minute", speedDemonUnlocked);
                        updateAchievementCard(achievementsGrid, 4, "All-Rounder", "Complete quizzes in 5 different categories", allRounderUnlocked);
                        updateAchievementCard(achievementsGrid, 5, "Persistence", "Retry a failed quiz and pass it", persistenceUnlocked);
                    }
                }
            }
        }
    }
    
    private void updateAchievementCard(JPanel achievementsGrid, int index, String title, String description, boolean unlocked) {
        if (index < achievementsGrid.getComponentCount()) {
            achievementsGrid.remove(index);
            achievementsGrid.add(createAchievementCard(title, description, unlocked), index);
            achievementsGrid.revalidate();
            achievementsGrid.repaint();
        }
    }
    
    private void saveProfile() {
        if (user != null) {
            user.setUsername(usernameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            
            profileController.updateUser(user);
            JOptionPane.showMessageDialog(this, 
                "Profile information updated successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh avatar with new initial
            getContentPane().getComponent(0).repaint();
        }
    }
} 