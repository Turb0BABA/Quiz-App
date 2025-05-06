package com.quizapp.controller;

import com.quizapp.model.Answer;
import com.quizapp.model.Category;
import com.quizapp.model.Question;
import com.quizapp.model.User;
import com.quizapp.service.AdminService;

import java.util.List;
import java.util.Optional;
import java.io.File;
import java.io.IOException;

public class AdminController {
    private final AdminService adminService;

    public AdminController() {
        this.adminService = new AdminService();
    }

    // Category CRUD
    public List<Category> getAllCategories() { return adminService.getAllCategories(); }
    public List<Category> getCategoriesWithQuestions() { return adminService.getCategoriesWithQuestions(); }
    public List<Category> getAllCategoriesWithSubcategories() { return adminService.getAllCategoriesWithSubcategories(); }
    public List<Category> getSubcategories(int parentId) { return adminService.getSubcategories(parentId); }
    public Optional<Category> getCategoryById(int id) { return adminService.getCategoryById(id); }
    public Category createCategory(Category c) { return adminService.createCategory(c); }
    public Category createSubcategory(Category c, int parentId) { return adminService.createSubcategory(c, parentId); }
    public void updateCategory(Category c) { adminService.updateCategory(c); }
    public void deleteCategory(int id) { adminService.deleteCategory(id); }
    public int cleanupDuplicateCategories() { return adminService.cleanupDuplicateCategories(); }

    // Question CRUD
    public List<Question> getQuestionsByCategory(int categoryId) { return adminService.getQuestionsByCategory(categoryId); }
    public Optional<Question> getQuestionById(int id) { return adminService.getQuestionById(id); }
    public Question createQuestion(Question q) { return adminService.createQuestion(q); }
    public void updateQuestion(Question q) { adminService.updateQuestion(q); }
    public void deleteQuestion(int id) { adminService.deleteQuestion(id); }
    public int cleanupDuplicateQuestions() { return adminService.cleanupDuplicateQuestions(); }

    // Answers for a question
    public List<Answer> getAnswersForQuestion(int questionId) { return adminService.getAnswersForQuestion(questionId); }
    
    // User management
    public List<User> getAllUsers() { return adminService.getAllUsers(); }
    public Optional<User> getUserById(int userId) { return adminService.getUserById(userId); }
    public void deleteUser(int userId) { adminService.deleteUser(userId); }
    public void updateUserStatus(int userId, boolean isActive) { adminService.updateUserStatus(userId, isActive); }
    
    // Import/Export functionality (delegating to AdminDashboardController)
    private com.quizapp.admin.controller.AdminDashboardController dashboardController = 
        new com.quizapp.admin.controller.AdminDashboardController();
    
    /**
     * Export questions to CSV format
     * @param categoryId the category ID to export questions for (or null for all categories)
     * @param outputFile the output file to write to
     * @return number of questions exported
     * @throws IOException if an I/O error occurs
     */
    public int exportQuestionsToCSV(Integer categoryId, File outputFile) throws IOException {
        return dashboardController.exportQuestionsToCSV(categoryId, outputFile);
    }
    
    /**
     * Export questions to JSON format
     * @param categoryId the category ID to export questions for (or null for all categories)
     * @param outputFile the output file to write to
     * @return number of questions exported
     * @throws IOException if an I/O error occurs
     */
    public int exportQuestionsToJSON(Integer categoryId, File outputFile) throws IOException {
        return dashboardController.exportQuestionsToJSON(categoryId, outputFile);
    }
    
    /**
     * Import questions from CSV format
     * @param inputFile the input file to read from
     * @param categoryId the category ID to import questions into
     * @return number of questions imported
     * @throws IOException if an I/O error occurs
     */
    public int importQuestionsFromCSV(File inputFile, int categoryId) throws IOException {
        return dashboardController.importQuestionsFromCSV(inputFile, categoryId);
    }
    
    /**
     * Import questions from JSON format
     * @param inputFile the input file to read from
     * @param categoryId the category ID to import questions into
     * @return number of questions imported
     * @throws IOException if an I/O error occurs
     */
    public int importQuestionsFromJSON(File inputFile, int categoryId) throws IOException {
        return dashboardController.importQuestionsFromJSON(inputFile, categoryId);
    }
} 