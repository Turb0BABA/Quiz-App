package com.quizapp.view.scoring;

import com.quizapp.dao.CategoryDAO;
import com.quizapp.dao.QuizResultDAO;
import com.quizapp.model.Category;
import com.quizapp.model.QuizResult;
import com.quizapp.util.UIConstants;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class LeaderboardPanel extends JPanel {
    private JComboBox<Category> categoryComboBox;
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    private final QuizResultDAO quizResultDAO;
    private final CategoryDAO categoryDAO;
    private JPanel topPerformersPanel;
    private JPanel statsPanel;
    private List<QuizResult> currentResults;
    private JLabel titleLabel;
    private JPanel filterPanel;
    private ButtonGroup periodGroup;
    private String currentPeriod = "All Time";

    public LeaderboardPanel() {
        this.quizResultDAO = new QuizResultDAO();
        this.categoryDAO = new CategoryDAO();
        
        setLayout(new BorderLayout(15, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        setBackground(UIConstants.BACKGROUND_COLOR);

        // Header panel with title and filters
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with top performers, table, and stats
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setOpaque(false);
        
        // Top performers panel (podium)
        topPerformersPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        topPerformersPanel.setOpaque(false);
        topPerformersPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Create table with modern styling
        createLeaderboardTable();
        
        // Stats panel for summary statistics
        statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Create a split pane with table in the center and stats at the bottom
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(new JScrollPane(leaderboardTable), BorderLayout.CENTER);
        tableContainer.add(statsPanel, BorderLayout.SOUTH);
        
        mainContent.add(topPerformersPanel, BorderLayout.NORTH);
        mainContent.add(tableContainer, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.CENTER);

        // Load data
        loadCategories();
        
        // Configure listeners
        categoryComboBox.addActionListener(e -> loadLeaderboard());
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        
        // Title with modern styling
        titleLabel = UIConstants.createHeaderLabel("Leaderboard");
        
        // Create filter panel with modern tabs instead of dropdown
        filterPanel = createFilterPanel();
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(filterPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Top row - Category and period selection
        JPanel topRow = new JPanel(new BorderLayout(20, 0));
        topRow.setOpaque(false);
        
        // Left side - Category selector with label
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        categoryPanel.setOpaque(false);
        
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font(categoryLabel.getFont().getName(), Font.BOLD, UIConstants.LABEL_FONT_SIZE));
        
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setPreferredSize(new Dimension(200, 30));
        categoryComboBox.setFont(new Font(categoryComboBox.getFont().getName(), Font.PLAIN, 14));
        
        categoryPanel.add(categoryLabel);
        categoryPanel.add(categoryComboBox);
        
        // Right side - Time period buttons
        JPanel periodPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        periodPanel.setOpaque(false);
        periodPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        periodGroup = new ButtonGroup();
        JToggleButton allTimeBtn = createPeriodToggleButton("All Time", true);
        JToggleButton monthBtn = createPeriodToggleButton("This Month", false);
        JToggleButton weekBtn = createPeriodToggleButton("This Week", false);
        JToggleButton todayBtn = createPeriodToggleButton("Today", false);
        
        periodPanel.add(todayBtn);
        periodPanel.add(weekBtn);
        periodPanel.add(monthBtn);
        periodPanel.add(allTimeBtn);
        
        topRow.add(categoryPanel, BorderLayout.WEST);
        topRow.add(periodPanel, BorderLayout.EAST);
        
        // Bottom row - Search and filter options
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Search field with icon
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setOpaque(false);
        
        JTextField searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.putClientProperty("JTextField.placeholderText", "Search users...");
        
        JButton searchButton = new JButton("🔍");
        searchButton.setFont(new Font(searchButton.getFont().getName(), Font.PLAIN, 14));
        searchButton.setBorderPainted(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setPreferredSize(new Dimension(30, 30));
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            filterLeaderboardTable(query);
        });
        
        JPanel searchInputPanel = new JPanel(new BorderLayout());
        searchInputPanel.setOpaque(false);
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);
        
        searchPanel.add(searchInputPanel);
        
        // Quick filter buttons
        JPanel quickFiltersPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        quickFiltersPanel.setOpaque(false);
        
        JButton top10Btn = new JButton("Top 10");
        top10Btn.setFont(new Font(top10Btn.getFont().getName(), Font.PLAIN, 12));
        top10Btn.setFocusPainted(false);
        top10Btn.addActionListener(e -> filterTopResults(10));
        
        JButton myRankBtn = new JButton("My Rank");
        myRankBtn.setFont(new Font(myRankBtn.getFont().getName(), Font.PLAIN, 12));
        myRankBtn.setFocusPainted(false);
        myRankBtn.addActionListener(e -> highlightUserRank());
        
        quickFiltersPanel.add(top10Btn);
        quickFiltersPanel.add(myRankBtn);
        
        bottomRow.add(searchPanel, BorderLayout.WEST);
        bottomRow.add(quickFiltersPanel, BorderLayout.EAST);
        
        panel.add(topRow);
        panel.add(bottomRow);
        
        return panel;
    }
    
    private JToggleButton createPeriodToggleButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text);
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setSelected(selected);
        periodGroup.add(button);
        
        button.addActionListener(e -> {
            currentPeriod = text;
            loadLeaderboard();
        });
        
        return button;
    }
    
    private void createLeaderboardTable() {
        // Modern table model with better column names
        tableModel = new DefaultTableModel(
            new Object[]{"Rank", "User", "Score", "Correct", "Questions", "Percentage", "Date"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class; // Rank
                if (column == 2) return Integer.class; // Score
                if (column == 3) return Integer.class; // Correct
                if (column == 4) return Integer.class; // Total
                if (column == 5) return Double.class;  // Percentage
                return Object.class;
            }
        };
        
        leaderboardTable = new JTable(tableModel);
        
        // Modern table styling
        leaderboardTable.setRowHeight(50);
        leaderboardTable.setShowGrid(false);
        leaderboardTable.setIntercellSpacing(new Dimension(0, 0));
        leaderboardTable.setFillsViewportHeight(true);
        leaderboardTable.setSelectionBackground(new Color(240, 247, 255));
        leaderboardTable.setSelectionForeground(UIConstants.TEXT_COLOR);
        
        // Custom header renderer with modern look
        JTableHeader header = leaderboardTable.getTableHeader();
        header.setFont(new Font(header.getFont().getName(), Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBackground(UIConstants.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220)));
        
        // Set column widths
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Rank
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(200); // User
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Score
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Correct
        leaderboardTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Total
        leaderboardTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Percentage
        leaderboardTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Date
        
        // Custom cell renderers for modern look
        leaderboardTable.setDefaultRenderer(Object.class, new LeaderboardCellRenderer());
        leaderboardTable.setDefaultRenderer(Integer.class, new LeaderboardCellRenderer());
        leaderboardTable.setDefaultRenderer(Double.class, new LeaderboardCellRenderer());
        
        // Add sorting capability
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        leaderboardTable.setRowSorter(sorter);
        
        // Add row hover effect
        leaderboardTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = leaderboardTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    leaderboardTable.setRowSelectionInterval(row, row);
                } else {
                    leaderboardTable.clearSelection();
                }
            }
        });
        
        leaderboardTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                leaderboardTable.clearSelection();
            }
        });
    }
    
    /**
     * Custom cell renderer for leaderboard table with modern styling
     */
    private class LeaderboardCellRenderer extends DefaultTableCellRenderer {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Remove border on focus
            ((JComponent)c).setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            
            // Base styling
            setHorizontalAlignment(column == 1 ? LEFT : CENTER);
            
            // Apply alternating row colors
            if (!isSelected) {
                if (row % 2 == 0) {
                    setBackground(UIConstants.CARD_BACKGROUND);
                } else {
                    setBackground(new Color(250, 250, 252));
                }
                setForeground(UIConstants.TEXT_COLOR);
            }
            
            // Special styling for rank column - Use medal icons for top 3
            if (column == 0 && value != null) {
                int rank = (int) value;
                setText(String.valueOf(rank));
                
                if (rank == 1) {
                    setIcon(createRankIcon("\uD83E\uDD47", new Color(255, 215, 0))); // Gold medal
                    setForeground(new Color(218, 165, 32));
                    setFont(new Font(getFont().getName(), Font.BOLD, 14));
                } else if (rank == 2) {
                    setIcon(createRankIcon("\uD83E\uDD48", new Color(192, 192, 192))); // Silver medal
                    setForeground(new Color(150, 150, 150)); 
                    setFont(new Font(getFont().getName(), Font.BOLD, 14));
                } else if (rank == 3) {
                    setIcon(createRankIcon("\uD83E\uDD49", new Color(205, 127, 50))); // Bronze medal
                    setForeground(new Color(184, 115, 51));
                    setFont(new Font(getFont().getName(), Font.BOLD, 14));
                } else {
                    setIcon(null);
                }
            } else {
                setIcon(null);
            }
            
            // Username styling (make bold)
            if (column == 1) {
                setFont(new Font(getFont().getName(), Font.BOLD, 14));
            }
            
            // Format percentages with color coding
            if (column == 5 && value != null) {
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
            
            // Format dates
            if (column == 6 && value instanceof Date) {
                setText(dateFormat.format((Date) value));
                setFont(new Font(getFont().getName(), Font.ITALIC, 12));
                setForeground(new Color(130, 130, 130));
            }
            
            return c;
        }
        
        private Icon createRankIcon(String symbol, Color color) {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(color);
                    
                    // Draw the medal symbol
                    g2d.setFont(new Font("Dialog", Font.BOLD, 16));
                    g2d.drawString(symbol, x, y + getIconHeight() - 4);
                    
                    g2d.dispose();
                }
                
                @Override
                public int getIconWidth() {
                    return 20;
                }
                
                @Override
                public int getIconHeight() {
                    return 20;
                }
            };
        }
    }
    
    /**
     * Creates a podium item for a top performer with modern card design
     */
    private JPanel createPodiumItem(QuizResult result, int rank) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create card with rounded corners
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(), 15, 15);
                
                // Create gradient based on rank
                Color startColor, endColor;
                switch (rank) {
                    case 1: // Gold
                        startColor = new Color(255, 215, 0, 30);
                        endColor = new Color(218, 165, 32, 10);
                        break;
                    case 2: // Silver
                        startColor = new Color(192, 192, 192, 30);
                        endColor = new Color(150, 150, 150, 10);
                        break;
                    case 3: // Bronze
                        startColor = new Color(205, 127, 50, 30);
                        endColor = new Color(184, 115, 51, 10);
                        break;
                    default:
                        startColor = new Color(240, 240, 240);
                        endColor = new Color(240, 240, 240);
                }
                
                GradientPaint gp = new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor);
                
                g2d.setPaint(gp);
                g2d.fill(roundedRect);
                
                // Draw card border
                g2d.setStroke(new BasicStroke(1f));
                g2d.setColor(new Color(230, 230, 230));
                g2d.draw(roundedRect);
                
                g2d.dispose();
            }
        };
        
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(5, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Medal ribbon at the top
        JPanel ribbonPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Choose medal color
                Color medalColor;
                String medalText;
                switch (rank) {
                    case 1:
                        medalColor = new Color(255, 215, 0);
                        medalText = "🥇 1st Place";
                        break;
                    case 2:
                        medalColor = new Color(192, 192, 192);
                        medalText = "🥈 2nd Place";
                        break;
                    case 3:
                        medalColor = new Color(205, 127, 50);
                        medalText = "🥉 3rd Place";
                        break;
                    default:
                        medalColor = UIConstants.PRIMARY_COLOR;
                        medalText = rank + "th Place";
                }
                
                // Draw ribbon text
                g2d.setColor(medalColor);
                g2d.setFont(new Font(getFont().getName(), Font.BOLD, 12));
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(medalText);
                g2d.drawString(medalText, (getWidth() - textWidth) / 2, 15);
                
                g2d.dispose();
            }
        };
        ribbonPanel.setPreferredSize(new Dimension(panel.getWidth(), 20));
        ribbonPanel.setOpaque(false);
        
        // Create avatar with circle and letter
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Determine medal color
                Color medalColor;
                switch (rank) {
                    case 1:
                        medalColor = new Color(255, 215, 0); // Gold
                        break;
                    case 2:
                        medalColor = new Color(192, 192, 192); // Silver
                        break;
                    case 3:
                        medalColor = new Color(205, 127, 50); // Bronze
                        break;
                    default:
                        medalColor = UIConstants.PRIMARY_COLOR;
                }
                
                // Draw avatar circle
                int size = Math.min(getWidth(), getHeight()) - 10;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillOval(x + 2, y + 2, size, size);
                
                // Draw circle background
                g2d.setColor(medalColor);
                g2d.fillOval(x, y, size, size);
                
                // Add a highlight effect
                g2d.setPaint(new GradientPaint(
                    x, y, new Color(255, 255, 255, 120),
                    x, y + size, new Color(255, 255, 255, 0)
                ));
                g2d.fillOval(x, y, size, size / 2);
                
                // Draw user initial
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(getFont().getName(), Font.BOLD, 32));
                String initial = result.getUsername().substring(0, 1).toUpperCase();
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(initial);
                int textHeight = fm.getHeight();
                g2d.drawString(initial, 
                    x + (size - textWidth) / 2, 
                    y + (size - textHeight) / 2 + fm.getAscent());
                
                g2d.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(80, 80));
        avatarPanel.setOpaque(false);
        
        // User info panel with name and score
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(result.getUsername(), SwingConstants.CENTER);
        nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel scoreLabel = new JLabel(result.getScore() + " pts", SwingConstants.CENTER);
        scoreLabel.setFont(new Font(scoreLabel.getFont().getName(), Font.BOLD, 20));
        
        // Choose medal color for score
        Color medalColor;
        switch (rank) {
            case 1:
                medalColor = new Color(255, 215, 0); // Gold
                break;
            case 2:
                medalColor = new Color(192, 192, 192); // Silver
                break;
            case 3:
                medalColor = new Color(205, 127, 50); // Bronze
                break;
            default:
                medalColor = UIConstants.PRIMARY_COLOR;
        }
        
        scoreLabel.setForeground(medalColor);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        double percentage = (double) result.getScore() / result.getTotalQuestions() * 100;
        JLabel percentLabel = new JLabel(String.format("%.1f%%", percentage), SwingConstants.CENTER);
        percentLabel.setFont(new Font(percentLabel.getFont().getName(), Font.PLAIN, 14));
        percentLabel.setForeground(new Color(100, 100, 100));
        percentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add a sparkline chart to show trend
        JPanel sparklinePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create sample sparkline data (would use real data in production)
                int[] data = generateSampleSparklineData(rank);
                
                // Draw sparkline
                g2d.setColor(medalColor);
                g2d.setStroke(new BasicStroke(1.5f));
                
                int width = getWidth() - 10;
                int height = getHeight() - 6;
                int x = 5;
                int y = 3;
                
                int stepX = width / (data.length - 1);
                int[] points = new int[data.length * 2];
                
                for (int i = 0; i < data.length; i++) {
                    points[i*2] = x + (i * stepX);
                    points[i*2+1] = y + height - ((data[i] * height) / 100);
                }
                
                for (int i = 0; i < data.length - 1; i++) {
                    g2d.drawLine(points[i*2], points[i*2+1], points[(i+1)*2], points[(i+1)*2+1]);
                }
                
                g2d.dispose();
            }
            
            private int[] generateSampleSparklineData(int rank) {
                // Generate random-ish data based on rank (higher ranks have better trends)
                int[] data = new int[7];
                int base = 50 + (4 - rank) * 10; // Higher base for better ranks
                
                for (int i = 0; i < data.length; i++) {
                    // Add some randomness but ensure general upward trend
                    data[i] = Math.min(100, Math.max(10, base + (i * 5) + (int)(Math.random() * 10) - 5));
                }
                
                return data;
            }
        };
        sparklinePanel.setPreferredSize(new Dimension(100, 20));
        sparklinePanel.setOpaque(false);
        sparklinePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(scoreLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(percentLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(sparklinePanel);
        
        // Assemble panel
        panel.add(ribbonPanel, BorderLayout.NORTH);
        panel.add(avatarPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        // Add hover effect
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                panel.repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                panel.repaint();
            }
        });
        
        return panel;
    }
    
    /**
     * Creates a stat item for the stats panel with visual representation
     */
    private JPanel createStatItem(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Add background with subtle gradient
        panel.setBackground(UIConstants.CARD_BACKGROUND);
        
        // Create header panel with icon and title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        headerPanel.setOpaque(false);
        
        // Custom icon based on stat type
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw icon based on title
                g2d.setColor(color);
                if (title.contains("Participants")) {
                    drawPeopleIcon(g2d);
                } else if (title.contains("Best")) {
                    drawTrophyIcon(g2d);
                } else if (title.contains("Average")) {
                    drawChartIcon(g2d);
                } else {
                    drawCheckmarkIcon(g2d);
                }
                
                g2d.dispose();
            }
            
            private void drawPeopleIcon(Graphics2D g2d) {
                // Simple people icon
                g2d.fillOval(8, 5, 8, 8); // Head
                g2d.fillRoundRect(6, 13, 12, 12, 4, 4); // Body
            }
            
            private void drawTrophyIcon(Graphics2D g2d) {
                // Simple trophy icon
                g2d.fillRoundRect(8, 5, 8, 12, 3, 3); // Cup
                g2d.fillRect(10, 17, 4, 5); // Base
                g2d.fillRect(7, 22, 10, 2); // Stand
            }
            
            private void drawChartIcon(Graphics2D g2d) {
                // Simple chart icon
                g2d.fillRect(5, 18, 4, 7); // Bar 1
                g2d.fillRect(10, 14, 4, 11); // Bar 2
                g2d.fillRect(15, 10, 4, 15); // Bar 3
            }
            
            private void drawCheckmarkIcon(Graphics2D g2d) {
                // Simple checkmark icon
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawLine(6, 14, 10, 18);
                g2d.drawLine(10, 18, 18, 8);
            }
        };
        iconLabel.setPreferredSize(new Dimension(24, 24));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));
        titleLabel.setForeground(new Color(80, 80, 80));
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        
        // Create value section
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font(valueLabel.getFont().getName(), Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        // Create visualization based on stat type
        JPanel vizPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Extract number for visualization
                int number = 0;
                String[] parts = value.split("[^0-9.]");
                try {
                    if (parts.length > 0 && !parts[0].isEmpty()) {
                        if (value.contains("%")) {
                            number = (int)Double.parseDouble(parts[0]);
                        } else if (value.contains("/")) {
                            String[] fractionParts = value.split("/");
                            int numerator = Integer.parseInt(fractionParts[0]);
                            int denominator = Integer.parseInt(fractionParts[1]);
                            number = denominator > 0 ? (numerator * 100) / denominator : 0;
                        } else {
                            number = Integer.parseInt(parts[0]);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Just use 0 if parsing fails
                }
                
                // Draw progress indicator based on stat type
                int width = getWidth() - 10;
                int height = 6;
                int x = 5;
                int y = (getHeight() - height) / 2;
                
                // Background bar
                g2d.setColor(new Color(230, 230, 230));
                g2d.fillRoundRect(x, y, width, height, height, height);
                
                // Progress bar
                int progressWidth = title.contains("Participants") ?
                    Math.min(width, Math.max(10, (number * width) / 20)) : // Scale for participants (assume max 20)
                    (number * width) / 100;  // Percentage-based for others
                
                if (progressWidth > 0) {
                    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
                    g2d.fillRoundRect(x, y, progressWidth, height, height, height);
                }
                
                g2d.dispose();
            }
        };
        vizPanel.setPreferredSize(new Dimension(panel.getWidth(), 30));
        vizPanel.setOpaque(false);
        
        // Add components to panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(valueLabel, BorderLayout.NORTH);
        contentPanel.add(vizPanel, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Add hover effect
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(UIConstants.CARD_HOVER_BACKGROUND);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(UIConstants.CARD_BACKGROUND);
            }
        });
        
        return panel;
    }
    
    /**
     * Refreshes the leaderboard data by reloading categories and leaderboard entries.
     */
    public void refreshData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadCategories();
                return null;
            }
        };
        worker.execute();
    }

    private void loadCategories() {
        List<Category> categories = categoryDAO.findAll();
        categoryComboBox.removeAllItems();
        
        // First, organize categories into main categories and subcategories
        Map<Integer, Category> mainCategories = new HashMap<>();
        Map<Integer, List<Category>> subcategories = new HashMap<>();
        
        for (Category c : categories) {
            if (!c.isSubcategory()) {
                mainCategories.put(c.getCategoryId(), c);
                subcategories.put(c.getCategoryId(), new ArrayList<>());
            } else if (c.getParentCategoryId() > 0) {
                List<Category> subs = subcategories.getOrDefault(c.getParentCategoryId(), new ArrayList<>());
                subs.add(c);
                subcategories.put(c.getParentCategoryId(), subs);
            }
        }
        
        // Use a custom renderer for the dropdown
        categoryComboBox.setRenderer(new CategoryListRenderer());
        
        // Add a "All Categories" option
        Category allCategories = new Category();
        allCategories.setCategoryId(0);
        allCategories.setName("All Categories");
        allCategories.setDescription("Show results from all categories");
        allCategories.setSubcategory(false);
        categoryComboBox.addItem(allCategories);
        
        // Add main categories first, then their subcategories
        for (Integer mainCatId : mainCategories.keySet()) {
            Category mainCategory = mainCategories.get(mainCatId);
            categoryComboBox.addItem(mainCategory);
            
            // Add subcategories with proper indentation
            List<Category> subs = subcategories.get(mainCatId);
            if (subs != null) {
                for (Category sub : subs) {
                    categoryComboBox.addItem(sub);
                }
            }
        }
        
        if (categories.size() > 0) {
            categoryComboBox.setSelectedIndex(0);
            loadLeaderboard();
        }
    }
    
    /**
     * Custom renderer to display categories and subcategories with proper formatting
     */
    private class CategoryListRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Category) {
                Category category = (Category) value;
                
                if (category.getCategoryId() == 0) {
                    // "All Categories" option
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setText("All Categories");
                    label.setIcon(null);
                } else if (category.isSubcategory()) {
                    // Subcategory - indented with bullet
                    label.setText("    • " + category.getName());
                    label.setFont(label.getFont().deriveFont(Font.PLAIN));
                    
                    // Set icon if we have one
                    if (category.getIconPath() != null) {
                        try {
                            ImageIcon icon = new ImageIcon(category.getIconPath());
                            Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                            label.setIcon(new ImageIcon(img));
                        } catch (Exception e) {
                            label.setIcon(null);
                        }
                    } else {
                        label.setIcon(null);
                    }
                } else {
                    // Main category - bold
                    label.setText(category.getName());
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    
                    // Set icon if we have one
                    if (category.getIconPath() != null) {
                        try {
                            ImageIcon icon = new ImageIcon(category.getIconPath());
                            Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                            label.setIcon(new ImageIcon(img));
                        } catch (Exception e) {
                            label.setIcon(null);
                        }
                    } else {
                        label.setIcon(null);
                    }
                }
            }
            
            // Add some padding
            label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            
            return label;
        }
    }
    
    private void loadLeaderboard() {
        Category selected = (Category) categoryComboBox.getSelectedItem();
        if (selected == null) return;
        
        // Show loading indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Update title with category name - handle the "All Categories" special case
        if (selected.getCategoryId() == 0) {
            titleLabel.setText("Leaderboard: All Categories");
            currentResults = quizResultDAO.getGlobalLeaderboard();
        } else {
            titleLabel.setText("Leaderboard: " + selected.getName());
            currentResults = quizResultDAO.getLeaderboard(selected.getCategoryId());
        }
        
        // Clear table
        tableModel.setRowCount(0);
        
        // Clear top performers panel
        topPerformersPanel.removeAll();
        
        // Clear stats panel
        statsPanel.removeAll();
        
        // Check if we have results
        if (currentResults.isEmpty()) {
            // Show empty state
            JPanel emptyStatePanel = new JPanel(new BorderLayout());
            emptyStatePanel.setOpaque(false);
            
            JLabel emptyIcon = new JLabel("\uD83D\uDCC8", SwingConstants.CENTER); // Chart emoji
            emptyIcon.setFont(new Font(emptyIcon.getFont().getName(), Font.PLAIN, 48));
            emptyIcon.setForeground(new Color(200, 200, 200));
            
            JLabel emptyLabel = new JLabel("No quiz results available for this category", SwingConstants.CENTER);
            emptyLabel.setFont(new Font(emptyLabel.getFont().getName(), Font.BOLD, 16));
            emptyLabel.setForeground(new Color(120, 120, 120));
            
            JLabel emptySubLabel = new JLabel("Be the first to complete a quiz in this category!", SwingConstants.CENTER);
            emptySubLabel.setFont(new Font(emptySubLabel.getFont().getName(), Font.PLAIN, 14));
            emptySubLabel.setForeground(new Color(150, 150, 150));
            
            JPanel textPanel = new JPanel(new BorderLayout(0, 5));
            textPanel.setOpaque(false);
            textPanel.add(emptyLabel, BorderLayout.NORTH);
            textPanel.add(emptySubLabel, BorderLayout.CENTER);
            
            emptyStatePanel.add(emptyIcon, BorderLayout.CENTER);
            emptyStatePanel.add(textPanel, BorderLayout.SOUTH);
            
            topPerformersPanel.setLayout(new BorderLayout());
            topPerformersPanel.add(emptyStatePanel, BorderLayout.CENTER);
        } else {
            // Reset layout if needed
            if (!(topPerformersPanel.getLayout() instanceof GridLayout)) {
                topPerformersPanel.setLayout(new GridLayout(1, 3, 15, 0));
            }
            
            // Add top 3 performers to podium
            int topCount = Math.min(3, currentResults.size());
            for (int i = 0; i < topCount; i++) {
                // Reorder podium: 2nd (left), 1st (center), 3rd (right)
                int index;
                if (i == 0) index = 1;      // 1st place in center
                else if (i == 1) index = 0; // 2nd place on left
                else index = 2;             // 3rd place on right
                
                QuizResult result = currentResults.get(i);
                JPanel podiumItem = createPodiumItem(result, i + 1);
                topPerformersPanel.add(podiumItem);
            }
            
            // Populate table with all results
            int rank = 1;
            int totalScore = 0;
            int totalQuestions = 0;
            int totalCorrect = 0;
            double bestScore = 0;
            
            for (QuizResult r : currentResults) {
                // Calculate percentage
                double percentage = 0;
                if (r.getTotalQuestions() > 0) {
                    percentage = (double) r.getScore() / r.getTotalQuestions() * 100;
                    
                    // Track stats
                    totalScore += r.getScore();
                    totalQuestions += r.getTotalQuestions();
                    int correct = (int) (r.getScore() / (100.0 / r.getTotalQuestions()));
                    totalCorrect += correct;
                    
                    if (percentage > bestScore) {
                        bestScore = percentage;
                    }
                    
                    // Add table row
                    tableModel.addRow(new Object[]{
                        rank++,
                        r.getUsername(),
                        r.getScore(),
                        correct,
                        r.getTotalQuestions(),
                        percentage,
                        r.getCompletedAt()
                    });
                }
            }
            
            // Add enhanced stats with visualizations
            double avgPercentage = totalQuestions > 0 ? (double) totalScore / totalQuestions * 100 : 0;
            
            statsPanel.add(createStatItem("Participants", String.valueOf(currentResults.size()), UIConstants.PRIMARY_COLOR));
            statsPanel.add(createStatItem("Best Score", String.format("%.1f%%", bestScore), UIConstants.SUCCESS_COLOR));
            statsPanel.add(createStatItem("Average Score", String.format("%.1f%%", avgPercentage), UIConstants.SECONDARY_COLOR));
            statsPanel.add(createStatItem("Correct Answers", totalCorrect + "/" + totalQuestions, UIConstants.ACCENT_COLOR));
        }
        
        // Reset cursor
        setCursor(Cursor.getDefaultCursor());
        
        // Refresh UI
        topPerformersPanel.revalidate();
        topPerformersPanel.repaint();
        statsPanel.revalidate();
        statsPanel.repaint();
        leaderboardTable.revalidate();
        leaderboardTable.repaint();
    }
    
    /**
     * Filters the leaderboard table by username
     */
    private void filterLeaderboardTable(String query) {
        if (query.isEmpty()) {
            loadLeaderboard(); // Reset to show all
            return;
        }
        
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) leaderboardTable.getRowSorter();
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1)); // Filter by username column
    }
    
    /**
     * Filters to show only top N results
     */
    private void filterTopResults(int limit) {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) leaderboardTable.getRowSorter();
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                int rowIndex = entry.getIdentifier();
                return rowIndex < limit;
            }
        });
    }
    
    /**
     * Highlights the current user's rank in the table
     * (In a real app, would use the actual logged-in user ID)
     */
    private void highlightUserRank() {
        // Reset filter first
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) leaderboardTable.getRowSorter();
        sorter.setRowFilter(null);
        
        // This is a mock implementation - in a real app we would know the current user
        // and scroll to their position in the table
        if (tableModel.getRowCount() > 0) {
            // For demo, just highlight a random row as the "current user"
            int userRow = Math.min(tableModel.getRowCount() - 1, 2);
            leaderboardTable.setRowSelectionInterval(userRow, userRow);
            leaderboardTable.scrollRectToVisible(leaderboardTable.getCellRect(userRow, 0, true));
            
            // Show a tooltip
            JOptionPane.showMessageDialog(
                this,
                "Your current rank is #" + (userRow + 1),
                "Your Ranking",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
} 