package com.quizapp.view.quiz;

import com.quizapp.controller.QuizController;
import com.quizapp.model.Category;
import com.quizapp.service.ApplicationEventManager;
import com.quizapp.service.ApplicationEventManager.EventType;
import com.quizapp.service.ApplicationEventManager.EventListener;
import com.quizapp.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CategorySelectionPanel extends JPanel implements EventListener {
    private final QuizController quizController;
    private final QuizStartListener quizStartListener;
    private JPanel categoriesPanel;
    private JPanel mainPanel;
    private JPanel subcategoriesPanel;
    private CardLayout cardLayout;
    private Category currentMainCategory;
    private final int userId;
    private JPanel breadcrumbPanel;
    private JLabel titleLabel;

    // Interface for callback when user selects a quiz category
    public interface QuizStartListener {
        void onQuizStart(Category selectedCategory);
    }

    public CategorySelectionPanel(QuizStartListener quizStartListener) {
        this(quizStartListener, 1); // Default to user ID 1 if none provided
    }
    
    public CategorySelectionPanel(QuizStartListener quizStartListener, int userId) {
        this.quizStartListener = quizStartListener;
        this.userId = userId;
        this.quizController = new QuizController(userId);
        
        // Register for category update events
        ApplicationEventManager.getInstance().addListener(EventType.CATEGORY_UPDATED, this);
        
        // Initialize UI
        initializeUI();
        
        // Load categories
        loadCategories();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
            UIConstants.STANDARD_PADDING, 
            UIConstants.STANDARD_PADDING, 
            UIConstants.STANDARD_PADDING, 
            UIConstants.STANDARD_PADDING
        ));
        setBackground(UIConstants.BACKGROUND_COLOR);

        // Create header with title and breadcrumb
        JPanel headerPanel = new JPanel(new BorderLayout(5, 10));
        headerPanel.setOpaque(false);
        
        // Initialize breadcrumb with home only
        breadcrumbPanel = UIConstants.createBreadcrumbPanel(new String[]{"Home", "Categories"});
        headerPanel.add(breadcrumbPanel, BorderLayout.NORTH);
        
        // Add title
        titleLabel = UIConstants.createHeaderLabel("Knowledge Library");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Add search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        searchPanel.setName("searchPanel"); // Add name to easily find it later
        
        JTextField searchField = new JTextField(20);
        searchField.setName("searchField"); // Add name to easily find it later
        searchField.putClientProperty("JTextField.placeholderText", "Search categories...");
        
        // Style the search field to look more modern
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JButton clearButton = new JButton("✕");
        clearButton.setFont(new Font(clearButton.getFont().getName(), Font.PLAIN, 12));
        clearButton.setForeground(UIConstants.TEXT_COLOR);
        clearButton.setBackground(UIConstants.BACKGROUND_COLOR);
        clearButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            // filterCategories(); // Disabled for now
        });
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(clearButton, BorderLayout.EAST);
        
        // Add search panel to header
        headerPanel.add(searchPanel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);

        // Create card layout for main categories and subcategories
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        mainPanel.setOpaque(false);
        
        // Create panel for main categories in a folder structure
        categoriesPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        categoriesPanel.setOpaque(false);
        
        // Create panel for subcategories
        subcategoriesPanel = new JPanel(new BorderLayout(10, 10));
        subcategoriesPanel.setOpaque(false);
        
        JPanel subcategoriesContent = new JPanel(new GridLayout(0, 3, 15, 15));
        subcategoriesContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        subcategoriesContent.setOpaque(false);
        
        // Create enhanced back button panel with room entry visual
        JPanel backButtonPanel = new JPanel(new BorderLayout(10, 10));
        backButtonPanel.setOpaque(false);
        backButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel backButtonWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonWrapper.setOpaque(false);
        
        JButton backButton = createDoorButton("← Return to Main Hall");
        UIConstants.addTooltip(backButton, "Return to main categories");
        backButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "categories");
            titleLabel.setText("Knowledge Library");
            updateBreadcrumb(null); // Reset breadcrumb
        });
        backButtonWrapper.add(backButton);
        
        JLabel roomLabel = new JLabel();
        roomLabel.setFont(new Font(roomLabel.getFont().getName(), Font.ITALIC, 14));
        roomLabel.setForeground(UIConstants.TEXT_SECONDARY);
        
        backButtonPanel.add(backButtonWrapper, BorderLayout.WEST);
        backButtonPanel.add(roomLabel, BorderLayout.EAST);
        
        subcategoriesPanel.add(backButtonPanel, BorderLayout.NORTH);
        subcategoriesPanel.add(new JScrollPane(subcategoriesContent) {
            {
                setBorder(null);
                getViewport().setOpaque(false);
                setOpaque(false);
            }
        }, BorderLayout.CENTER);
        
        // Add panels to card layout
        JScrollPane categoriesScrollPane = new JScrollPane(categoriesPanel) {
            {
                setBorder(null);
                getViewport().setOpaque(false);
                setOpaque(false);
            }
        };
        
        mainPanel.add(categoriesScrollPane, "categories");
        mainPanel.add(subcategoriesPanel, "subcategories");
        
        // Show main categories by default
        cardLayout.show(mainPanel, "categories");
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Handle events from the ApplicationEventManager
     */
    @Override
    public void onEvent(EventType eventType, Object data) {
        if (eventType == EventType.CATEGORY_UPDATED) {
            // Use SwingUtilities to ensure UI updates happen on EDT
            SwingUtilities.invokeLater(this::refreshCategories);
        }
    }
    
    /**
     * Public method to refresh categories data from outside
     */
    public void refreshCategories() {
        loadCategories();
    }
    
    /**
     * Creates a button styled like a door to represent category entry
     */
    private JButton createDoorButton(String text) {
        return UIConstants.createDoorButton(text, true);
    }
    
    /**
     * Updates the breadcrumb navigation based on current location
     */
    private void updateBreadcrumb(Category category) {
        // Create new breadcrumb panel
        JPanel newBreadcrumbPanel;
        
        if (category == null) {
            // Main categories view
            newBreadcrumbPanel = UIConstants.createBreadcrumbPanel(new String[]{"Home", "Categories"});
        } else {
            // Subcategories view
            newBreadcrumbPanel = UIConstants.createBreadcrumbPanel(new String[]{"Home", "Categories", category.getName()});
        }
        
        // Get header panel and replace breadcrumb
        JPanel headerPanel = (JPanel) getComponent(0);
        headerPanel.remove(0); // Remove old breadcrumb
        headerPanel.add(newBreadcrumbPanel, BorderLayout.NORTH);
        
        // Update reference to current breadcrumb
        breadcrumbPanel = newBreadcrumbPanel;
        
        // Refresh UI
        headerPanel.revalidate();
        headerPanel.repaint();
    }

    private void loadCategories() {
        SwingWorker<List<Category>, Void> worker = new SwingWorker<List<Category>, Void>() {
            @Override
            protected List<Category> doInBackground() {
                return quizController.getAllCategoriesWithSubcategories();
            }
            
            @Override
            protected void done() {
                try {
                    List<Category> mainCategories = get();
                    displayMainCategories(mainCategories);
        } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        CategorySelectionPanel.this,
                "Error loading categories: " + e.getMessage(),
                "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayMainCategories(List<Category> mainCategories) {
        categoriesPanel.removeAll();
        
        if (mainCategories.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setOpaque(false);
            JLabel emptyLabel = new JLabel("No quiz categories available", JLabel.CENTER);
            emptyLabel.setFont(new Font(emptyLabel.getFont().getName(), Font.ITALIC, 16));
            emptyLabel.setForeground(UIConstants.TEXT_SECONDARY);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            categoriesPanel.add(emptyPanel);
        } else {
            for (Category category : mainCategories) {
                JPanel categoryCard = createFolderCard(category, true);
                categoriesPanel.add(categoryCard);
            }
        }
        
        categoriesPanel.revalidate();
        categoriesPanel.repaint();
    }
    
    private void displaySubcategories(Category mainCategory) {
        currentMainCategory = mainCategory;
        
        // Update title to show we're in a specific room
        titleLabel.setText(mainCategory.getName() + " Room");
        
        // Update breadcrumb
        updateBreadcrumb(mainCategory);

        // Get the content panel from the subcategories panel
        JScrollPane scrollPane = (JScrollPane) subcategoriesPanel.getComponent(1);
        JPanel subcategoriesContent = (JPanel) scrollPane.getViewport().getView();
        subcategoriesContent.removeAll();

        // Get the back button panel
        JPanel backButtonPanel = (JPanel) subcategoriesPanel.getComponent(0);
        JPanel backButtonWrapper = (JPanel) backButtonPanel.getComponent(0);
        JLabel roomLabel = (JLabel) backButtonPanel.getComponent(1);
        roomLabel.setText("Exploring: " + mainCategory.getName());
        
        // Add subcategories
        List<Category> subcategories = mainCategory.getSubcategories();
        
        if (subcategories.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setOpaque(false);
            JLabel emptyLabel = new JLabel("No subcategories available for " + mainCategory.getName(), JLabel.CENTER);
            emptyLabel.setFont(new Font(emptyLabel.getFont().getName(), Font.ITALIC, 16));
            emptyLabel.setForeground(UIConstants.TEXT_SECONDARY);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            subcategoriesContent.add(emptyPanel);
        } else {
            for (Category subcategory : subcategories) {
                JPanel subcategoryCard = createFolderCard(subcategory, false);
                subcategoriesContent.add(subcategoryCard);
            }
        }
        
        subcategoriesContent.revalidate();
        subcategoriesContent.repaint();
        
        // Show subcategories panel
        cardLayout.show(mainPanel, "subcategories");
    }
    
    private JPanel createFolderCard(Category category, boolean isMainCategory) {
        // Create a folder-like card with rounded corners and shadow effect
        JPanel card = new JPanel(new BorderLayout(5, 10));
        card.setBorder(UIConstants.FOLDER_BORDER);
        card.setBackground(UIConstants.CARD_BACKGROUND);
        
        // Create header panel with folder icon and title
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        
        // Create folder icon
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(48, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Apply a folder background to the icon
        iconLabel.setOpaque(true);
        iconLabel.setBackground(isMainCategory ? UIConstants.FOLDER_MAIN_BG : UIConstants.FOLDER_SUB_BG);
        iconLabel.setForeground(isMainCategory ? UIConstants.PRIMARY_COLOR : UIConstants.SECONDARY_COLOR);
        iconLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isMainCategory ? UIConstants.PRIMARY_COLOR : UIConstants.SECONDARY_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        if (category.getIconPath() != null) {
            try {
                ImageIcon icon = new ImageIcon(category.getIconPath());
                Image img = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                iconLabel.setText(category.getName().substring(0, 1).toUpperCase());
                iconLabel.setFont(new Font(iconLabel.getFont().getName(), Font.BOLD, 20));
            }
        } else {
            iconLabel.setText(category.getName().substring(0, 1).toUpperCase());
            iconLabel.setFont(new Font(iconLabel.getFont().getName(), Font.BOLD, 20));
        }
        
        // Create title and subtitle panel with folder paradigm
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        
        // Title with more prominence
        JLabel nameLabel = new JLabel(category.getName());
        nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.BOLD, 
            isMainCategory ? UIConstants.SUB_HEADER_FONT_SIZE : UIConstants.LABEL_FONT_SIZE));
        nameLabel.setForeground(UIConstants.TEXT_COLOR);
        
        // Type label as subtitle
        JLabel typeLabel = new JLabel(isMainCategory ? "Room" : "Section");
        typeLabel.setFont(new Font(typeLabel.getFont().getName(), Font.ITALIC, 12));
        typeLabel.setForeground(new Color(120, 120, 120));
        
        titlePanel.add(nameLabel);
        titlePanel.add(typeLabel);
        
        headerPanel.add(iconLabel, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        
        // Description with slightly larger font and padding
        JTextArea descLabel = new JTextArea(category.getDescription());
        descLabel.setFont(new Font(descLabel.getFont().getName(), Font.PLAIN, 13));
        descLabel.setForeground(UIConstants.TEXT_COLOR);
        descLabel.setWrapStyleWord(true);
        descLabel.setLineWrap(true);
        descLabel.setEditable(false);
        descLabel.setOpaque(false);
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
        
        // Add statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Question count statistic
        JPanel questionsStats = createStatPanel("Questions", 
            String.valueOf(category.getQuestionCount() > 0 ? category.getQuestionCount() : "N/A"), 
            UIConstants.PRIMARY_COLOR);
        
        // Difficulty level statistic
        String difficultyText = "Beginner";
        Color difficultyColor = UIConstants.SUCCESS_COLOR;
        if (category.getDifficultyLevel() > 0) {
            if (category.getDifficultyLevel() == 1) {
                difficultyText = "Beginner";
                difficultyColor = UIConstants.SUCCESS_COLOR;
            } else if (category.getDifficultyLevel() == 2) {
                difficultyText = "Intermediate";
                difficultyColor = UIConstants.WARNING_COLOR;
            } else {
                difficultyText = "Advanced";
                difficultyColor = UIConstants.ERROR_COLOR;
            }
        }
        JPanel difficultyStats = createStatPanel("Difficulty", difficultyText, difficultyColor);
        
        // Time statistic
        int timePerQuestion = category.getTimePerQuestion() > 0 ? category.getTimePerQuestion() : 30;
        JPanel timeStats = createStatPanel("Time", timePerQuestion + " sec/q", UIConstants.ACCENT_COLOR);
        
        statsPanel.add(questionsStats);
        statsPanel.add(difficultyStats);
        statsPanel.add(timeStats);
        
        contentPanel.add(descLabel, BorderLayout.NORTH);
        contentPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Create button panel with door entry styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JButton actionButton;
        if (isMainCategory && category.hasSubcategories()) {
            // Create a door-like button for entering category rooms
            actionButton = UIConstants.createDoorButton("Enter Room", true);
            try {
                actionButton.setIcon(new ImageIcon(getClass().getResource("/icons/door.png")));
            } catch (Exception e) {
                // If icon fails to load, just use text
            }
            
            UIConstants.addTooltip(actionButton, "Enter the " + category.getName() + " room");
            actionButton.addActionListener(e -> displaySubcategories(category));
        } else {
            // Create a quiz start button for leaf categories
            actionButton = UIConstants.createDoorButton("Start Quiz", false);
            try {
                actionButton.setIcon(new ImageIcon(getClass().getResource("/icons/start.png")));
            } catch (Exception e) {
                // If icon fails to load, just use text
            }
            
            UIConstants.addTooltip(actionButton, "Begin a quiz in " + category.getName());
            actionButton.addActionListener(e -> quizStartListener.onQuizStart(category));
        }
        
        buttonPanel.add(actionButton);
        
        // Assemble card components
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add folder-like hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(isMainCategory ? 
                    UIConstants.FOLDER_MAIN_HOVER : 
                    UIConstants.FOLDER_SUB_HOVER
                );
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(0, 0, 7, 7), // Increased shadow effect
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                            isMainCategory ? UIConstants.PRIMARY_COLOR : UIConstants.SECONDARY_COLOR, 
                            1, true
                        ),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                    )
                ));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(UIConstants.CARD_BACKGROUND);
                card.setBorder(UIConstants.FOLDER_BORDER);
            }
        });
        
        return card;
    }
    
    /**
     * Creates a statistic display panel with a value and label
     */
    private JPanel createStatPanel(String label, String value, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setOpaque(false);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font(valueLabel.getFont().getName(), Font.BOLD, 14));
        valueLabel.setForeground(accentColor);
        
        JLabel nameLabel = new JLabel(label, SwingConstants.CENTER);
        nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.PLAIN, 11));
        nameLabel.setForeground(new Color(120, 120, 120));
        
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void filterCategories() {
        // Find the search field in the header panel
        JPanel headerPanel = (JPanel) getComponent(0);
        if (headerPanel == null) return;
        
        JPanel searchPanel = null;
        // Look for the search panel among header components
        for (Component comp : headerPanel.getComponents()) {
            if (comp instanceof JPanel && comp.getName() != null && "searchPanel".equals(comp.getName())) {
                searchPanel = (JPanel) comp;
                break;
            }
            // The search panel might be the third component (index 2)
            if (headerPanel.getComponentCount() > 2 && comp == headerPanel.getComponent(2) && comp instanceof JPanel) {
                searchPanel = (JPanel) comp;
                break;
            }
        }
        
        if (searchPanel == null) return;
        
        // Find text field in search panel
        JTextField searchField = null;
        for (Component comp : searchPanel.getComponents()) {
            if (comp instanceof JTextField) {
                searchField = (JTextField) comp;
                break;
            }
        }
        
        if (searchField == null) return;
        
        String searchText = searchField.getText().toLowerCase();
        
        // Get the categories panel
        JScrollPane scrollPane = (JScrollPane) mainPanel.getComponent(0);
        JPanel categoriesPanel = (JPanel) scrollPane.getViewport().getView();
        
        // Filter categories
        for (Component comp : categoriesPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                boolean matches = false;
                
                // Search in all text components in the card
                for (Component cardComp : getAllComponents(card)) {
                    if (cardComp instanceof JLabel) {
                        String text = ((JLabel) cardComp).getText();
                        if (text != null && text.toLowerCase().contains(searchText)) {
                            matches = true;
                            break;
                        }
                    } else if (cardComp instanceof JTextArea) {
                        String text = ((JTextArea) cardComp).getText();
                        if (text != null && text.toLowerCase().contains(searchText)) {
                            matches = true;
                            break;
                        }
                    }
                }
                
                // If search is empty, show all cards
                if (searchText.isEmpty()) {
                    matches = true;
                }
                
                card.setVisible(matches);
            }
        }
        
        categoriesPanel.revalidate();
        categoriesPanel.repaint();
    }
    
    /**
     * Helper method to get all components in a container recursively
     */
    private java.util.List<Component> getAllComponents(Container container) {
        java.util.List<Component> components = new java.util.ArrayList<>();
        for (Component comp : container.getComponents()) {
            components.add(comp);
            if (comp instanceof Container) {
                components.addAll(getAllComponents((Container) comp));
            }
        }
        return components;
    }

    public void setMobileMode(boolean mobile) {
        // Scale font sizes
        Font titleFont = mobile ? new Font(Font.SANS_SERIF, Font.BOLD, 28) : new Font(Font.SANS_SERIF, Font.BOLD, 18);
        Font cardFont = mobile ? new Font(Font.SANS_SERIF, Font.BOLD, 22) : new Font(Font.SANS_SERIF, Font.BOLD, 14);
        int cardHeight = mobile ? 80 : 48;
        
        // Try to update title label (might be in the header panel)
        Component header = getComponent(0);
        if (header instanceof Container) {
            Container headerPanel = (Container)header;
            if (headerPanel.getComponentCount() > 1) {
                Component titleComp = headerPanel.getComponent(1);
                if (titleComp instanceof JLabel) {
                    JLabel titleLabel = (JLabel)titleComp;
                    titleLabel.setFont(titleFont);
                }
            }
        }
        
        // Update category cards - handle carefully
        if (mainPanel != null && mainPanel.getComponentCount() > 0) {
            Component scrollPaneComp = mainPanel.getComponent(0);
            if (scrollPaneComp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)scrollPaneComp;
                Component viewComp = scrollPane.getViewport().getView();
                if (viewComp instanceof Container) {
                    Container categoriesPanel = (Container)viewComp;
                    
                    // Loop through card components and update fonts/sizes
                    for (Component comp : categoriesPanel.getComponents()) {
                        if (comp instanceof JPanel) {
                            JPanel card = (JPanel)comp;
                            card.setPreferredSize(new Dimension(card.getPreferredSize().width, cardHeight));
                            
                            // Try to find title labels in cards
                            updateFontsInContainer(card, cardFont);
                        }
                    }
                }
            }
        }
        
        // Refresh UI
        revalidate();
        repaint();
    }
    
    /**
     * Helper method to update fonts in containers recursively
     */
    private void updateFontsInContainer(Container container, Font font) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                // Update title labels
                JLabel label = (JLabel)comp;
                if (label.getFont().getStyle() == Font.BOLD) {
                    label.setFont(font);
                }
            } else if (comp instanceof Container) {
                // Recursively process nested containers
                updateFontsInContainer((Container)comp, font);
            }
        }
    }

    /**
     * Clean up when this panel is no longer needed
     */
    public void cleanup() {
        // Unregister from event manager to prevent memory leaks
        ApplicationEventManager.getInstance().removeListener(EventType.CATEGORY_UPDATED, this);
    }
} 