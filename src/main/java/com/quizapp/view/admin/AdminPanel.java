package com.quizapp.view.admin;

import com.quizapp.controller.AdminController;
import com.quizapp.model.Category;
import com.quizapp.model.Question;
import com.quizapp.model.User;
import com.quizapp.service.ApplicationEventManager;
import com.quizapp.service.ApplicationEventManager.EventType;
import com.quizapp.util.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * JPanel containing the administrator tools for managing questions and categories.
 */
public class AdminPanel extends JPanel {

    private final AdminController adminController;
    private DefaultTableModel categoryTableModel;
    private DefaultTableModel questionTableModel;
    private DefaultTableModel userTableModel;
    private JTable categoryTable;
    private JTable questionTable;
    private JTable userTable;
    private JComboBox<Category> categoryFilterComboBox;
    private JTabbedPane tabbedPane;
    private JPanel userManagementPanel;

    public AdminPanel() {
        this.adminController = new AdminController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Categories", createCategoryPanel());
        tabbedPane.addTab("Questions", createQuestionPanel());
        tabbedPane.addTab("User Management", createUserManagementPanel());
        tabbedPane.addTab("Analytics", createAnalyticsPanel());
        tabbedPane.addTab("Import/Export", createImportExportPanel());
        add(tabbedPane, BorderLayout.CENTER);
        
        // Register a change listener to load users when the user tab is selected
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 2) { // User Management tab
                loadUsers();
            }
        });
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // Category table model with more columns
        categoryTableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Description", "Type", "Questions", "Time/Q", "Total Time"}, 0
        ) {
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5 || columnIndex == 6) return Integer.class;
                return String.class;
            }
        };
        
        categoryTable = new JTable(categoryTableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.setRowHeight(30);
        
        // Set column widths
        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Name
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(250);  // Description
        categoryTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Type
        categoryTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // Questions
        categoryTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // Time/Q
        categoryTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // Total Time

        panel.add(new JScrollPane(categoryTable), BorderLayout.CENTER);
        
        // Enhanced button panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        
        // Left side buttons for category types
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addMainBtn = new JButton("Add Main Category");
        JButton addSubBtn = new JButton("Add Subcategory");
        leftButtons.add(addMainBtn);
        leftButtons.add(addSubBtn);
        
        // Right side buttons for actions
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("Edit");
        JButton settingsBtn = new JButton("Quiz Settings");
        JButton delBtn = new JButton("Delete");
        JButton cleanupBtn = new JButton("Clean Up");
        rightButtons.add(editBtn);
        rightButtons.add(settingsBtn);
        rightButtons.add(delBtn);
        rightButtons.add(cleanupBtn);
        
        buttonPanel.add(leftButtons, BorderLayout.WEST);
        buttonPanel.add(rightButtons, BorderLayout.EAST);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addMainBtn.addActionListener(e -> addMainCategory());
        addSubBtn.addActionListener(e -> addSubcategory());
        editBtn.addActionListener(e -> editCategory());
        settingsBtn.addActionListener(e -> editQuizSettings());
        delBtn.addActionListener(e -> deleteCategory());
        cleanupBtn.addActionListener(e -> cleanupDuplicateCategories());
        
                    loadCategories();
        return panel;
    }

    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Category:"));
        categoryFilterComboBox = new JComboBox<>();
        topPanel.add(categoryFilterComboBox);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Enhanced question table
        questionTableModel = new DefaultTableModel(
            new Object[]{"ID", "Question", "Difficulty", "Points"}, 0
        ) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        questionTable = new JTable(questionTableModel);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setRowHeight(30);
        
        // Set column widths
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(400);  // Question
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Difficulty
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(80);   // Points
        
        panel.add(new JScrollPane(questionTable), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton cleanupBtn = new JButton("Clean Up Questions");
        buttonPanel.add(addBtn); 
        buttonPanel.add(editBtn); 
        buttonPanel.add(delBtn);
        buttonPanel.add(cleanupBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addQuestion());
        editBtn.addActionListener(e -> editQuestion());
        delBtn.addActionListener(e -> deleteQuestion());
        cleanupBtn.addActionListener(e -> cleanupDuplicateQuestions());
        categoryFilterComboBox.addActionListener(e -> loadQuestions());
        
        loadCategoriesForFilter();
        return panel;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create user table
        userTableModel = new DefaultTableModel(
            new Object[]{"ID", "Username", "Email", "Full Name", "Admin", "Last Login"}, 0
        ) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(30);
        
        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Username
        userTable.getColumnModel().getColumn(2).setPreferredWidth(200);  // Email
        userTable.getColumnModel().getColumn(3).setPreferredWidth(200);  // Full Name
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Admin
        userTable.getColumnModel().getColumn(5).setPreferredWidth(150);  // Last Login
        
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        
        // User management buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewProfileBtn = new JButton("View Profile");
        JButton deleteUserBtn = new JButton("Delete User");
        JButton refreshBtn = new JButton("Refresh");
        
        buttonPanel.add(viewProfileBtn);
        buttonPanel.add(deleteUserBtn);
        buttonPanel.add(refreshBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        viewProfileBtn.addActionListener(e -> viewUserProfile());
        deleteUserBtn.addActionListener(e -> deleteUser());
        refreshBtn.addActionListener(e -> loadUsers());
        
        return panel;
    }
    
    private void loadCategories() {
        SwingWorker<List<Category>, Void> worker = new SwingWorker<List<Category>, Void>() {
            @Override
            protected List<Category> doInBackground() {
                return adminController.getAllCategoriesWithSubcategories();
            }
            
            @Override
            protected void done() {
                try {
                    List<Category> categories = get();
                    categoryTableModel.setRowCount(0);
                    
                    // Add main categories first
                    for (Category mainCategory : categories) {
                        int questionCount = countQuestionsInCategory(mainCategory.getCategoryId());
                        categoryTableModel.addRow(new Object[]{
                            mainCategory.getCategoryId(),
                            mainCategory.getName(),
                            mainCategory.getDescription(),
                            "Main Category",
                            questionCount,
                            mainCategory.getTimePerQuestion(),
                            mainCategory.getTotalTime()
                        });
                        
                        // Add subcategories
                        for (Category subCategory : mainCategory.getSubcategories()) {
                            int subQuestionCount = countQuestionsInCategory(subCategory.getCategoryId());
                            categoryTableModel.addRow(new Object[]{
                                subCategory.getCategoryId(),
                                "  • " + subCategory.getName(),
                                subCategory.getDescription(),
                                "Subcategory",
                                subQuestionCount,
                                subCategory.getTimePerQuestion(),
                                subCategory.getTotalTime()
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        AdminPanel.this,
                        "Error loading categories: " + e.getMessage(),
                    "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    private int countQuestionsInCategory(int categoryId) {
        return adminController.getQuestionsByCategory(categoryId).size();
    }

    private void loadCategoriesForFilter() {
        List<Category> categories = adminController.getAllCategories();
        categoryFilterComboBox.removeAllItems();
        for (Category c : categories) {
            categoryFilterComboBox.addItem(c);
        }
        if (categories.size() > 0) {
            categoryFilterComboBox.setSelectedIndex(0);
                    loadQuestions();
                }
            }

    private void loadQuestions() {
        Category selected = (Category) categoryFilterComboBox.getSelectedItem();
        if (selected == null) return;
        List<Question> questions = adminController.getQuestionsByCategory(selected.getCategoryId());
        questionTableModel.setRowCount(0);
        for (Question q : questions) {
            questionTableModel.addRow(new Object[]{
                q.getQuestionId(), 
                q.getQuestionText(),
                q.getDifficultyLevel(),
                q.getPoints()
            });
        }
    }

    private void addMainCategory() {
        JTextField nameField = new JTextField(20);
        JTextField descField = new JTextField(30);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Category Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Add Main Category", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String description = descField.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Category name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Category c = new Category();
            c.setName(name);
            c.setDescription(description);
            c.setSubcategory(false);
            c.setDisplayOrder(0);  // Default display order
            
            adminController.createCategory(c);
            loadCategories();
            loadCategoriesForFilter();
            
            // Notify other components that categories have been updated
            ApplicationEventManager.getInstance().fireEvent(EventType.CATEGORY_UPDATED, null);
            
            JOptionPane.showMessageDialog(
                this, 
                "Main category created successfully.", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    private void addSubcategory() {
        // First, get all main categories
        List<Category> mainCategories = adminController.getAllCategoriesWithSubcategories();
        
        if (mainCategories.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "You must create a main category first before adding subcategories.",
                "No Main Categories",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Create input fields
        JComboBox<Category> parentComboBox = new JComboBox<>();
        for (Category c : mainCategories) {
            parentComboBox.addItem(c);
        }
        
        JTextField nameField = new JTextField(20);
        JTextField descField = new JTextField(30);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Parent Category:"));
        panel.add(parentComboBox);
        panel.add(new JLabel("Subcategory Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Add Subcategory", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            Category parentCategory = (Category) parentComboBox.getSelectedItem();
            String name = nameField.getText().trim();
            String description = descField.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Subcategory name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (parentCategory == null) {
                JOptionPane.showMessageDialog(this, "Parent category must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Category subcategory = new Category();
            subcategory.setName(name);
            subcategory.setDescription(description);
            
            adminController.createSubcategory(subcategory, parentCategory.getCategoryId());
            loadCategories();
            loadCategoriesForFilter();
            
            // Notify other components that categories have been updated
            ApplicationEventManager.getInstance().fireEvent(EventType.CATEGORY_UPDATED, null);
            
            JOptionPane.showMessageDialog(
                this, 
                "Subcategory created successfully under " + parentCategory.getName(), 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void editCategory() {
        int row = categoryTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) categoryTableModel.getValueAt(row, 0);
        String currentName = (String) categoryTableModel.getValueAt(row, 1);
        String currentDesc = (String) categoryTableModel.getValueAt(row, 2);
        
        // Remove the bullet point if it's a subcategory
        if (currentName.startsWith("  • ")) {
            currentName = currentName.substring(4);
        }
        
        JTextField nameField = new JTextField(currentName, 20);
        JTextField descField = new JTextField(currentDesc, 30);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Category Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Edit Category", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String description = descField.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Category name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Category c = adminController.getCategoryById(id).orElse(null);
            if (c != null) {
                c.setName(name);
                c.setDescription(description);
                adminController.updateCategory(c);
                loadCategories();
                loadCategoriesForFilter();
                
                // Notify other components that categories have been updated
                ApplicationEventManager.getInstance().fireEvent(EventType.CATEGORY_UPDATED, null);
                
                JOptionPane.showMessageDialog(
                    this, 
                    "Category updated successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }
    
    private void editQuizSettings() {
        int row = categoryTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category to edit quiz settings.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) categoryTableModel.getValueAt(row, 0);
        int currentTimePerQ = (int) categoryTableModel.getValueAt(row, 5);
        int currentTotalTime = (int) categoryTableModel.getValueAt(row, 6);
        String categoryName = (String) categoryTableModel.getValueAt(row, 1);
        
        // Remove the bullet point if it's a subcategory
        if (categoryName.startsWith("  • ")) {
            categoryName = categoryName.substring(4);
        }
        
        // Create spinner models
        SpinnerNumberModel timePerQModel = new SpinnerNumberModel(currentTimePerQ, 5, 300, 5);
        SpinnerNumberModel totalTimeModel = new SpinnerNumberModel(currentTotalTime, 30, 7200, 30);
        
        // Create spinners
        JSpinner timePerQSpinner = new JSpinner(timePerQModel);
        JSpinner totalTimeSpinner = new JSpinner(totalTimeModel);
        
        // Create panel
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Category:"));
        panel.add(new JLabel(categoryName));
        panel.add(new JLabel("Time per Question (seconds):"));
        panel.add(timePerQSpinner);
        panel.add(new JLabel("Total Quiz Time (seconds):"));
        panel.add(totalTimeSpinner);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Edit Quiz Settings", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            int timePerQ = (int) timePerQSpinner.getValue();
            int totalTime = (int) totalTimeSpinner.getValue();
            
            Category c = adminController.getCategoryById(id).orElse(null);
            if (c != null) {
                c.setTimePerQuestion(timePerQ);
                c.setTotalTime(totalTime);
                adminController.updateCategory(c);
                loadCategories();
                
                // Notify other components that categories have been updated
                ApplicationEventManager.getInstance().fireEvent(EventType.CATEGORY_UPDATED, null);
                
                JOptionPane.showMessageDialog(
                    this, 
                    "Quiz settings updated successfully for " + categoryName,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    private void deleteCategory() {
        int row = categoryTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) categoryTableModel.getValueAt(row, 0);
        String categoryType = (String) categoryTableModel.getValueAt(row, 3);
        String categoryName = (String) categoryTableModel.getValueAt(row, 1);
        
        // Remove the bullet point if it's a subcategory
        if (categoryName.startsWith("  • ")) {
            categoryName = categoryName.substring(4);
        }
        
        String message;
        if (categoryType.equals("Main Category")) {
            message = "Are you sure you want to delete the main category \"" + categoryName + "\"?\n" + 
                       "This will also delete all its subcategories and questions!";
            } else {
            message = "Are you sure you want to delete the subcategory \"" + categoryName + "\"?\n" +
                       "This will also delete all questions in this subcategory!";
        }
        
        int confirmResult = JOptionPane.showConfirmDialog(
            this, message, "Confirm Delete", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
        );
        
        if (confirmResult == JOptionPane.YES_OPTION) {
            adminController.deleteCategory(id);
            loadCategories();
            loadCategoriesForFilter();
            
            // Notify other components that categories have been updated
            ApplicationEventManager.getInstance().fireEvent(EventType.CATEGORY_UPDATED, null);
            
            JOptionPane.showMessageDialog(
                this, 
                "Category deleted successfully.", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    private void cleanupDuplicateCategories() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "This will remove duplicate categories, keeping only the one with the most questions for each name.\nContinue?",
            "Clean Up Categories",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                int deletedCount = adminController.cleanupDuplicateCategories();
                JOptionPane.showMessageDialog(
                    this,
                    "Clean up complete! " + deletedCount + " duplicate categories removed.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
                // Refresh the views
                loadCategories();
                loadCategoriesForFilter();
                
                // Notify other components that categories have been updated
                ApplicationEventManager.getInstance().fireEvent(EventType.CATEGORY_UPDATED, null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error during category cleanup: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void addQuestion() {
        Category selected = (Category) categoryFilterComboBox.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a category first.", "No Category Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Template dropdown
        String[] templates = {"Multiple Choice", "True/False", "Fill in the Blank"};
        JComboBox<String> templateCombo = new JComboBox<>(templates);
        templateCombo.setSelectedIndex(0);
        // Question fields
        JTextField questionField = new JTextField(30);
        String[] difficulties = {"EASY", "MEDIUM", "HARD"};
        JComboBox<String> difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setSelectedItem("MEDIUM");
        SpinnerNumberModel pointsModel = new SpinnerNumberModel(10, 1, 100, 1);
        JSpinner pointsSpinner = new JSpinner(pointsModel);
        // Option fields
        JTextField option1Field = new JTextField(30);
        JTextField option2Field = new JTextField(30);
        JTextField option3Field = new JTextField(30);
        JTextField option4Field = new JTextField(30);
        JRadioButton radio1 = new JRadioButton("Correct");
        JRadioButton radio2 = new JRadioButton();
        JRadioButton radio3 = new JRadioButton();
        JRadioButton radio4 = new JRadioButton();
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(radio1);
        radioGroup.add(radio2);
        radioGroup.add(radio3);
        radioGroup.add(radio4);
        radio1.setSelected(true);
        // True/False fields
        JRadioButton trueRadio = new JRadioButton("True");
        JRadioButton falseRadio = new JRadioButton("False");
        ButtonGroup tfGroup = new ButtonGroup();
        tfGroup.add(trueRadio);
        tfGroup.add(falseRadio);
        trueRadio.setSelected(true);
        // Fill in the blank field
        JTextField fillBlankField = new JTextField(30);
        // Main panel
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Template:"));
        panel.add(templateCombo);
        panel.add(new JLabel("Question Text:"));
        panel.add(questionField);
        JPanel diffPointsPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        diffPointsPanel.add(new JLabel("Difficulty:"));
        diffPointsPanel.add(difficultyCombo);
        diffPointsPanel.add(new JLabel("Points:"));
        diffPointsPanel.add(pointsSpinner);
        panel.add(diffPointsPanel);
        // Option panels
        JPanel option1Panel = new JPanel(new BorderLayout(5, 0));
        option1Panel.add(radio1, BorderLayout.WEST);
        option1Panel.add(option1Field, BorderLayout.CENTER);
        JPanel option2Panel = new JPanel(new BorderLayout(5, 0));
        option2Panel.add(radio2, BorderLayout.WEST);
        option2Panel.add(option2Field, BorderLayout.CENTER);
        JPanel option3Panel = new JPanel(new BorderLayout(5, 0));
        option3Panel.add(radio3, BorderLayout.WEST);
        option3Panel.add(option3Field, BorderLayout.CENTER);
        JPanel option4Panel = new JPanel(new BorderLayout(5, 0));
        option4Panel.add(radio4, BorderLayout.WEST);
        option4Panel.add(option4Field, BorderLayout.CENTER);
        // True/False panel
        JPanel tfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfPanel.add(trueRadio);
        tfPanel.add(falseRadio);
        // Fill in the blank panel
        JPanel fillPanel = new JPanel(new BorderLayout(5, 0));
        fillPanel.add(new JLabel("Correct Answer:"), BorderLayout.WEST);
        fillPanel.add(fillBlankField, BorderLayout.CENTER);
        // Add initial (Multiple Choice) options
        panel.add(new JLabel("Options:"));
        panel.add(option1Panel);
        panel.add(option2Panel);
        panel.add(option3Panel);
        panel.add(option4Panel);
        // Template switching logic
        templateCombo.addActionListener(e -> {
            panel.removeAll();
            panel.add(new JLabel("Template:"));
            panel.add(templateCombo);
            panel.add(new JLabel("Question Text:"));
            panel.add(questionField);
            panel.add(diffPointsPanel);
            String selectedTemplate = (String) templateCombo.getSelectedItem();
            if ("Multiple Choice".equals(selectedTemplate)) {
                panel.add(new JLabel("Options:"));
                panel.add(option1Panel);
                panel.add(option2Panel);
                panel.add(option3Panel);
                panel.add(option4Panel);
            } else if ("True/False".equals(selectedTemplate)) {
                panel.add(new JLabel("Answer:"));
                panel.add(tfPanel);
            } else if ("Fill in the Blank".equals(selectedTemplate)) {
        panel.add(new JLabel("Correct Answer:"));
                panel.add(fillPanel);
            }
            panel.revalidate();
            panel.repaint();
        });
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Add Question to Category: " + selected.getName(), 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            String questionText = questionField.getText().trim();
            String selectedTemplate = (String) templateCombo.getSelectedItem();
            List<String> options = new ArrayList<>();
            int correctIndex = 0;
            if ("Multiple Choice".equals(selectedTemplate)) {
                String option1 = option1Field.getText().trim();
                String option2 = option2Field.getText().trim();
                String option3 = option3Field.getText().trim();
                String option4 = option4Field.getText().trim();
                if (questionText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Question text cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (option1.isEmpty() || option2.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "At least two options must be provided.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                options.add(option1);
                options.add(option2);
                if (!option3.isEmpty()) options.add(option3);
                if (!option4.isEmpty()) options.add(option4);
                correctIndex = radio1.isSelected() ? 0 : (radio2.isSelected() ? 1 : (radio3.isSelected() ? 2 : 3));
                if (correctIndex >= options.size()) correctIndex = 0;
            } else if ("True/False".equals(selectedTemplate)) {
                options.add("True");
                options.add("False");
                correctIndex = trueRadio.isSelected() ? 0 : 1;
                if (questionText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Question text cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if ("Fill in the Blank".equals(selectedTemplate)) {
                String answer = fillBlankField.getText().trim();
                if (questionText.isEmpty() || answer.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Both question and answer are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                options.add(answer);
                correctIndex = 0;
            }
            Question q = new Question();
            q.setCategoryId(selected.getCategoryId());
            q.setQuestionText(questionText);
            q.setOptions(options);
            q.setCorrectOptionIndex(correctIndex);
            q.setDifficultyLevel((String) difficultyCombo.getSelectedItem());
            q.setPoints((int) pointsSpinner.getValue());
            adminController.createQuestion(q);
            loadQuestions();
            JOptionPane.showMessageDialog(
                this, 
                "Question added successfully.", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void editQuestion() {
        int row = questionTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) questionTableModel.getValueAt(row, 0);
        String text = JOptionPane.showInputDialog(this, "Edit question:", questionTableModel.getValueAt(row, 1));
        if (text != null && !text.trim().isEmpty()) {
            Question q = adminController.getQuestionById(id).orElse(null);
            if (q != null) {
                q.setQuestionText(text.trim());
                adminController.updateQuestion(q);
                loadQuestions();
            }
        }
    }

    private void deleteQuestion() {
        int row = questionTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) questionTableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete this question?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            adminController.deleteQuestion(id);
            loadQuestions();
        }
    }

    private void cleanupDuplicateQuestions() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "This will remove duplicate questions, keeping only one copy of each question within the same category.\nContinue?",
            "Clean Up Questions",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                int deletedCount = adminController.cleanupDuplicateQuestions();
                JOptionPane.showMessageDialog(
                    this,
                    "Clean up complete! " + deletedCount + " duplicate questions removed.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
                // Refresh the questions view
                loadQuestions();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error during question cleanup: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() {
                return adminController.getAllUsers();
            }
            
            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    userTableModel.setRowCount(0);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    
                    for (User user : users) {
                        String lastLogin = user.getLastLogin() != null ? 
                            dateFormat.format(user.getLastLogin()) : "Never";
                        
                        userTableModel.addRow(new Object[]{
                            user.getUserId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getFullName(),
                            user.isAdmin() ? "Yes" : "No",
                            lastLogin
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                        AdminPanel.this,
                        "Error loading users: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }
    
    private void viewUserProfile() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a user to view.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        int userId = (int) userTable.getValueAt(selectedRow, 0);
        adminController.getUserById(userId).ifPresent(user -> {
            StringBuilder profileInfo = new StringBuilder();
            profileInfo.append("User ID: ").append(user.getUserId()).append("\n");
            profileInfo.append("Username: ").append(user.getUsername()).append("\n");
            profileInfo.append("Email: ").append(user.getEmail()).append("\n");
            profileInfo.append("Full Name: ").append(user.getFullName() != null ? user.getFullName() : "Not provided").append("\n");
            profileInfo.append("Admin: ").append(user.isAdmin() ? "Yes" : "No").append("\n");
            profileInfo.append("Created: ").append(user.getCreatedAt()).append("\n");
            profileInfo.append("Last Login: ").append(user.getLastLogin()).append("\n");
            
            JOptionPane.showMessageDialog(
                this,
                profileInfo.toString(),
                "User Profile: " + user.getUsername(),
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
    
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a user to delete.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        int userId = (int) userTable.getValueAt(selectedRow, 0);
        String username = (String) userTable.getValueAt(selectedRow, 1);
        
        // Prevent deleting the admin user
        if (username.equals("admin")) {
            JOptionPane.showMessageDialog(
                this,
                "The admin user cannot be deleted.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the user '" + username + "'?\nThis action cannot be undone.",
            "Confirm User Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                adminController.deleteUser(userId);
                JOptionPane.showMessageDialog(
                    this,
                    "User '" + username + "' has been deleted.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
                loadUsers();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    "Error deleting user: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create a dashboard panel with multiple analysis components
        JPanel dashboardPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        
        // Summary statistics panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary Statistics"));
        JPanel statsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        // Add some placeholder statistics
        JLabel totalQuizzesLabel = new JLabel("Total Quizzes Taken: Loading...");
        JLabel avgScoreLabel = new JLabel("Average Score: Loading...");
        JLabel completionRateLabel = new JLabel("Completion Rate: Loading...");
        
        statsPanel.add(totalQuizzesLabel);
        statsPanel.add(avgScoreLabel);
        statsPanel.add(completionRateLabel);
        
        summaryPanel.add(statsPanel, BorderLayout.NORTH);
        dashboardPanel.add(summaryPanel);
        
        // Category performance panel - placeholder for a chart
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Category Performance"));
        JLabel categoryChartLabel = new JLabel("Chart loading...", JLabel.CENTER);
        categoryPanel.add(categoryChartLabel, BorderLayout.CENTER);
        dashboardPanel.add(categoryPanel);
        
        // Time trend panel - placeholder for a chart
        JPanel trendPanel = new JPanel(new BorderLayout());
        trendPanel.setBorder(BorderFactory.createTitledBorder("Quiz Activity Over Time"));
        JLabel trendChartLabel = new JLabel("Chart loading...", JLabel.CENTER);
        trendPanel.add(trendChartLabel, BorderLayout.CENTER);
        dashboardPanel.add(trendPanel);
        
        // User performance panel
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("User Performance"));
        JLabel userChartLabel = new JLabel("Chart loading...", JLabel.CENTER);
        userPanel.add(userChartLabel, BorderLayout.CENTER);
        dashboardPanel.add(userPanel);
        
        // Add the dashboard to the main panel
        panel.add(dashboardPanel, BorderLayout.CENTER);
        
        // Add control panel with filters and refresh button
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Time Range:"));
        
        JComboBox<String> timeRangeCombo = new JComboBox<>(new String[] {
                "Last 7 Days", "Last 30 Days", "Last 90 Days", "All Time"
        });
        controlPanel.add(timeRangeCombo);
        
        controlPanel.add(new JLabel("Category:"));
        JComboBox<String> categoryCombo = new JComboBox<>(new String[] {"All Categories"});
        controlPanel.add(categoryCombo);
        
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> loadAnalyticsData(
                timeRangeCombo.getSelectedItem().toString(),
                categoryCombo.getSelectedItem().toString(),
                totalQuizzesLabel, avgScoreLabel, completionRateLabel,
                categoryChartLabel, trendChartLabel, userChartLabel));
        controlPanel.add(refreshButton);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Load initial data (this would normally be done in a SwingWorker)
        SwingUtilities.invokeLater(() -> loadAnalyticsData(
                "Last 30 Days", "All Categories",
                totalQuizzesLabel, avgScoreLabel, completionRateLabel,
                categoryChartLabel, trendChartLabel, userChartLabel));
        
        return panel;
    }
    
    private void loadAnalyticsData(String timeRange, String category,
                                  JLabel totalQuizzesLabel, JLabel avgScoreLabel, JLabel completionRateLabel,
                                  JLabel categoryChartLabel, JLabel trendChartLabel, JLabel userChartLabel) {
        // This would normally load real data using SwingWorker
        // For now, we're just setting placeholder data
        
        // In a real implementation, create an AdminDashboardController and use it to fetch data
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Simulate loading data
                Thread.sleep(1000);
                return null;
            }
            
            @Override
            protected void done() {
                // Update summary statistics
                totalQuizzesLabel.setText("Total Quizzes Taken: 487");
                avgScoreLabel.setText("Average Score: 76.3%");
                completionRateLabel.setText("Completion Rate: 94.2%");
                
                // Create and display charts
                // In a real implementation, use JFreeChart to create real charts
                
                // For category performance, create a pie chart
                String categoryChartHtml = "<html><body><div style='text-align:center;'>" +
                        "<div style='color:blue;font-size:10px;'>Science: 42%</div>" +
                        "<div style='color:red;font-size:10px;'>History: 28%</div>" +
                        "<div style='color:green;font-size:10px;'>Math: 20%</div>" +
                        "<div style='color:orange;font-size:10px;'>Literature: 10%</div>" +
                        "</div></body></html>";
                categoryChartLabel.setText(categoryChartHtml);
                
                // For time trend, create a line chart representation
                String trendChartHtml = "<html><body><div style='text-align:center;'>" +
                        "<div style='font-size:10px;'>Apr: 45 quizzes</div>" +
                        "<div style='font-size:10px;'>May: 78 quizzes</div>" +
                        "<div style='font-size:10px;'>Jun: 120 quizzes</div>" +
                        "<div style='font-size:10px;'>Jul: 244 quizzes</div>" +
                        "</div></body></html>";
                trendChartLabel.setText(trendChartHtml);
                
                // For user performance, create a bar chart representation
                String userChartHtml = "<html><body><div style='text-align:center;'>" +
                        "<div style='font-size:10px;'>Top Users by Score:</div>" +
                        "<div style='font-size:10px;'>Alice: 92%</div>" +
                        "<div style='font-size:10px;'>Bob: 88%</div>" +
                        "<div style='font-size:10px;'>Charlie: 83%</div>" +
                        "</div></body></html>";
                userChartLabel.setText(userChartHtml);
            }
        };
        
        worker.execute();
    }

    private JPanel createImportExportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create file operation panel
        JPanel filePanel = new JPanel(new GridLayout(2, 1, 0, 10));
        filePanel.setBorder(BorderFactory.createTitledBorder("File Operations"));
        
        // Import panel
        JPanel importPanel = new JPanel(new BorderLayout(5, 5));
        importPanel.setBorder(BorderFactory.createTitledBorder("Import Questions"));
        
        JPanel importTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        importTopPanel.add(new JLabel("Category:"));
        JComboBox<Category> importCategoryCombo = new JComboBox<>();
        
        // Fill with categories
        List<Category> categories = adminController.getAllCategories();
        for (Category category : categories) {
            importCategoryCombo.addItem(category);
        }
        
        importTopPanel.add(importCategoryCombo);
        importTopPanel.add(new JLabel("File:"));
        JTextField importFileField = new JTextField(30);
        importFileField.setEditable(false);
        importTopPanel.add(importFileField);
        JButton browseImportButton = new JButton("Browse...");
        importTopPanel.add(browseImportButton);
        
        JPanel importButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton importButton = new JButton("Import Questions");
        importButtonPanel.add(importButton);
        
        importPanel.add(importTopPanel, BorderLayout.NORTH);
        importPanel.add(importButtonPanel, BorderLayout.SOUTH);
        
        // Export panel
        JPanel exportPanel = new JPanel(new BorderLayout(5, 5));
        exportPanel.setBorder(BorderFactory.createTitledBorder("Export Questions"));
        
        JPanel exportTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        exportTopPanel.add(new JLabel("Category:"));
        JComboBox<Category> exportCategoryCombo = new JComboBox<>();
        
        // Add "All Categories" option
        Category allCategories = new Category();
        allCategories.setCategoryId(-1);
        allCategories.setName("All Categories");
        exportCategoryCombo.addItem(allCategories);
        
        // Fill with categories
        for (Category category : categories) {
            exportCategoryCombo.addItem(category);
        }
        
        exportTopPanel.add(exportCategoryCombo);
        exportTopPanel.add(new JLabel("Format:"));
        JComboBox<String> formatCombo = new JComboBox<>(new String[]{"CSV", "JSON"});
        exportTopPanel.add(formatCombo);
        exportTopPanel.add(new JLabel("File:"));
        JTextField exportFileField = new JTextField(30);
        exportFileField.setEditable(false);
        exportTopPanel.add(exportFileField);
        JButton browseExportButton = new JButton("Browse...");
        exportTopPanel.add(browseExportButton);
        
        JPanel exportButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Export Questions");
        exportButtonPanel.add(exportButton);
        
        exportPanel.add(exportTopPanel, BorderLayout.NORTH);
        exportPanel.add(exportButtonPanel, BorderLayout.SOUTH);
        
        filePanel.add(importPanel);
        filePanel.add(exportPanel);
        
        panel.add(filePanel, BorderLayout.NORTH);
        
        // Progress panel
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.setBorder(BorderFactory.createTitledBorder("Operation Progress"));
        
        JTextArea logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        
        progressPanel.add(logScrollPane, BorderLayout.CENTER);
        progressPanel.add(progressBar, BorderLayout.SOUTH);
        
        panel.add(progressPanel, BorderLayout.CENTER);
        
        // Add action listeners
        browseImportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV and JSON files", "csv", "json"));
            
            int result = fileChooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                importFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        browseExportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            
            String format = (String) formatCombo.getSelectedItem();
            if ("CSV".equals(format)) {
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "CSV files (*.csv)", "csv"));
            } else {
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "JSON files (*.json)", "json"));
            }
            
            int result = fileChooser.showSaveDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                
                // Add extension if missing
                if ("CSV".equals(format) && !path.toLowerCase().endsWith(".csv")) {
                    path += ".csv";
                } else if ("JSON".equals(format) && !path.toLowerCase().endsWith(".json")) {
                    path += ".json";
                }
                
                exportFileField.setText(path);
            }
        });
        
        importButton.addActionListener(e -> {
            String filePath = importFileField.getText();
            if (filePath.isEmpty()) {
                JOptionPane.showMessageDialog(panel, 
                    "Please select a file to import.", 
                    "No File Selected", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Category selectedCategory = (Category) importCategoryCombo.getSelectedItem();
            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(panel, 
                    "Please select a category for import.", 
                    "No Category Selected", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            File file = new File(filePath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(panel, 
                    "The selected file does not exist.", 
                    "File Not Found", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Determine file type
            boolean isCsv = filePath.toLowerCase().endsWith(".csv");
            boolean isJson = filePath.toLowerCase().endsWith(".json");
            
            if (!isCsv && !isJson) {
                JOptionPane.showMessageDialog(panel, 
                    "Please select a CSV or JSON file for import.", 
                    "Invalid File Type", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Disable buttons during import
            importButton.setEnabled(false);
            exportButton.setEnabled(false);
            progressBar.setValue(0);
            logArea.setText("");
            
            // Get values for the worker before creating it (to satisfy "effectively final" requirement)
            final File fileToImport = file;
            final int categoryIdToUse = selectedCategory.getCategoryId();
            final boolean isCsvFormat = isCsv;
            
            // Create a background worker for the import
            SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    publish("Starting import from " + fileToImport.getName() + "...");
                    
                    try {
                        int count;
                        if (isCsvFormat) {
                            publish("Importing from CSV format...");
                            count = adminController.importQuestionsFromCSV(fileToImport, categoryIdToUse);
                        } else {
                            publish("Importing from JSON format...");
                            count = adminController.importQuestionsFromJSON(fileToImport, categoryIdToUse);
                        }
                        return count;
                    } catch (Exception ex) {
                        publish("Error during import: " + ex.getMessage());
                        throw ex;
                    }
                }
                
                @Override
                protected void process(List<String> chunks) {
                    for (String chunk : chunks) {
                        logArea.append(chunk + "\n");
                    }
                }
                
                @Override
                protected void done() {
                    try {
                        int count = get();
                        progressBar.setValue(100);
                        logArea.append("Import completed successfully. " + count + " questions imported.\n");
                        JOptionPane.showMessageDialog(panel, 
                            count + " questions were successfully imported.", 
                            "Import Complete", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        progressBar.setValue(0);
                        logArea.append("Import failed: " + ex.getMessage() + "\n");
                        JOptionPane.showMessageDialog(panel, 
                            "Import failed: " + ex.getMessage(), 
                            "Import Error", 
                            JOptionPane.ERROR_MESSAGE);
                    } finally {
                        importButton.setEnabled(true);
                        exportButton.setEnabled(true);
                    }
                }
            };
            
            worker.execute();
        });
        
        exportButton.addActionListener(e -> {
            String filePath = exportFileField.getText();
            if (filePath.isEmpty()) {
                JOptionPane.showMessageDialog(panel, 
                    "Please select a file for export.", 
                    "No File Selected", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Category selectedCategory = (Category) exportCategoryCombo.getSelectedItem();
            Integer categoryId = selectedCategory.getCategoryId();
            if (categoryId == -1) {
                categoryId = null; // Export all categories
            }
            
            String format = (String) formatCombo.getSelectedItem();
            boolean isCsv = "CSV".equals(format);
            
            // Disable buttons during export
            importButton.setEnabled(false);
            exportButton.setEnabled(false);
            progressBar.setValue(0);
            logArea.setText("");
            
            // Get values for the worker before creating it (to satisfy "effectively final" requirement)
            final File fileToExport = new File(filePath);
            final Integer categoryIdToUse = categoryId;
            final boolean isCsvFormat = isCsv;
            
            // Create a background worker for the export
            SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    publish("Starting export to " + fileToExport.getName() + "...");
                    
                    try {
                        int count;
                        if (isCsvFormat) {
                            publish("Exporting to CSV format...");
                            count = adminController.exportQuestionsToCSV(categoryIdToUse, fileToExport);
                        } else {
                            publish("Exporting to JSON format...");
                            count = adminController.exportQuestionsToJSON(categoryIdToUse, fileToExport);
                        }
                        return count;
                    } catch (Exception ex) {
                        publish("Error during export: " + ex.getMessage());
                        throw ex;
                    }
                }
                
                @Override
                protected void process(List<String> chunks) {
                    for (String chunk : chunks) {
                        logArea.append(chunk + "\n");
                    }
                }
                
                @Override
                protected void done() {
                    try {
                        int count = get();
                        progressBar.setValue(100);
                        logArea.append("Export completed successfully. " + count + " questions exported.\n");
                        JOptionPane.showMessageDialog(panel, 
                            count + " questions were successfully exported.", 
                            "Export Complete", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        progressBar.setValue(0);
                        logArea.append("Export failed: " + ex.getMessage() + "\n");
                        JOptionPane.showMessageDialog(panel, 
                            "Export failed: " + ex.getMessage(), 
                            "Export Error", 
                            JOptionPane.ERROR_MESSAGE);
                    } finally {
                        importButton.setEnabled(true);
                        exportButton.setEnabled(true);
                    }
                }
            };
            
            worker.execute();
        });
        
        return panel;
    }
}