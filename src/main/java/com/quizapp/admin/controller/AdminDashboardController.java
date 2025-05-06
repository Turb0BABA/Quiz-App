package com.quizapp.admin.controller;

import com.quizapp.admin.service.AdminAnalyticsService;
import com.quizapp.admin.service.ImportExportService;
import com.quizapp.admin.service.UserManagementService;
import com.quizapp.admin.model.QuizAttempt;
import com.quizapp.model.Category;
import com.quizapp.model.Question;
import com.quizapp.model.User;
import com.quizapp.dao.CategoryDAO;

import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for admin dashboard functionality
 */
public class AdminDashboardController {
    private final AdminAnalyticsService analyticsService;
    private final ImportExportService importExportService;
    private final UserManagementService userManagementService;
    private final CategoryDAO categoryDAO;
    
    public AdminDashboardController() {
        this.analyticsService = new AdminAnalyticsService();
        this.importExportService = new ImportExportService();
        this.userManagementService = new UserManagementService();
        this.categoryDAO = new CategoryDAO();
    }
    
    // Analytics functionality
    
    /**
     * Get quiz attempts for a specific user
     * @param userId the user ID
     * @return list of quiz attempts
     */
    public List<QuizAttempt> getQuizAttemptsByUser(int userId) {
        return analyticsService.getAttemptsByUser(userId);
    }
    
    /**
     * Get all quiz attempts
     * @return list of all quiz attempts
     */
    public List<QuizAttempt> getAllQuizAttempts() {
        return analyticsService.getAllAttempts();
    }
    
    /**
     * Get completion counts by category
     * @return map of category name to completion count
     */
    public Map<String, Integer> getCompletionCountsByCategory() {
        return analyticsService.getCompletionCountsByCategory();
    }
    
    /**
     * Get average scores by category
     * @return map of category name to average score
     */
    public Map<String, Double> getAverageScoresByCategory() {
        return analyticsService.getAverageScoresByCategory();
    }
    
    /**
     * Get top categories by completion count
     * @param limit maximum number of categories to return
     * @return list of category entries (name, count) sorted by count (highest first)
     */
    public List<Map.Entry<String, Integer>> getTopCategoriesByCompletions(int limit) {
        return analyticsService.getTopCategoriesByCompletions(limit);
    }
    
    /**
     * Get quiz attempt counts by date for the past month
     * @return map of date to attempt count
     */
    public Map<LocalDate, Integer> getAttemptCountsForPastMonth() {
        return analyticsService.getAttemptCountsForPastMonth();
    }
    
    /**
     * Create pie chart for category completion counts
     * @return JFreeChart pie chart
     */
    public JFreeChart createCategoryCompletionPieChart() {
        return analyticsService.createCategoryCompletionPieChart();
    }
    
    /**
     * Create bar chart for average scores by category
     * @return JFreeChart bar chart
     */
    public JFreeChart createCategoryScoresBarChart() {
        return analyticsService.createCategoryScoresBarChart();
    }
    
    /**
     * Create line chart for quiz attempts over time
     * @return JFreeChart line chart
     */
    public JFreeChart createAttemptsOverTimeChart() {
        return analyticsService.createAttemptsOverTimeChart();
    }
    
    // Import/Export functionality
    
    /**
     * Export questions to CSV format
     * @param categoryId the category ID to export questions for (or null for all categories)
     * @param outputFile the output file to write to
     * @return number of questions exported
     * @throws IOException if an I/O error occurs
     */
    public int exportQuestionsToCSV(Integer categoryId, File outputFile) throws IOException {
        return importExportService.exportQuestionsToCSV(categoryId, outputFile);
    }
    
    /**
     * Export questions to JSON format
     * @param categoryId the category ID to export questions for (or null for all categories)
     * @param outputFile the output file to write to
     * @return number of questions exported
     * @throws IOException if an I/O error occurs
     */
    public int exportQuestionsToJSON(Integer categoryId, File outputFile) throws IOException {
        return importExportService.exportQuestionsToJSON(categoryId, outputFile);
    }
    
    /**
     * Import questions from CSV format
     * @param inputFile the input file to read from
     * @param categoryId the category ID to import questions into
     * @return number of questions imported
     * @throws IOException if an I/O error occurs
     */
    public int importQuestionsFromCSV(File inputFile, int categoryId) throws IOException {
        return importExportService.importQuestionsFromCSV(inputFile, categoryId);
    }
    
    /**
     * Import questions from JSON format
     * @param inputFile the input file to read from
     * @param categoryId the category ID to import questions into
     * @return number of questions imported
     * @throws IOException if an I/O error occurs
     */
    public int importQuestionsFromJSON(File inputFile, int categoryId) throws IOException {
        return importExportService.importQuestionsFromJSON(inputFile, categoryId);
    }
    
    /**
     * Check if a file has CSV extension
     * @param file the file to check
     * @return true if the file has CSV extension
     */
    public boolean isCSVFile(File file) {
        return importExportService.isCSVFile(file);
    }
    
    /**
     * Check if a file has JSON extension
     * @param file the file to check
     * @return true if the file has JSON extension
     */
    public boolean isJSONFile(File file) {
        return importExportService.isJSONFile(file);
    }
    
    // User Management functionality
    
    /**
     * Get all users
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userManagementService.getAllUsers();
    }
    
    /**
     * Get user by ID
     * @param userId the user ID
     * @return optional containing the user if found
     */
    public Optional<User> getUserById(int userId) {
        return userManagementService.getUserById(userId);
    }
    
    /**
     * Update a user's information
     * @param user the user to update
     * @return true if update succeeded
     */
    public boolean updateUser(User user) {
        return userManagementService.updateUser(user);
    }
    
    /**
     * Delete a user
     * @param userId the user ID
     * @return true if deletion succeeded
     */
    public boolean deleteUser(int userId) {
        return userManagementService.deleteUser(userId);
    }
    
    /**
     * Activate or deactivate a user
     * @param userId the user ID
     * @param isActive true to activate, false to deactivate
     * @return true if update succeeded
     */
    public boolean updateUserActiveStatus(int userId, boolean isActive) {
        return userManagementService.updateUserActiveStatus(userId, isActive);
    }
    
    /**
     * Generate a password reset token for a user
     * @param userId the user ID
     * @return the generated password reset token, or null if failed
     */
    public String generatePasswordResetToken(int userId) {
        return userManagementService.generatePasswordResetToken(userId);
    }
    
    /**
     * Force reset a user's password (administrative function)
     * @param userId the user ID
     * @return the newly generated password, or null if failed
     */
    public String forceResetPassword(int userId) {
        return userManagementService.forceResetPassword(userId);
    }
    
    /**
     * Toggle a user's admin status
     * @param userId the user ID
     * @param isAdmin true to grant admin, false to revoke
     * @return true if update succeeded
     */
    public boolean toggleAdminStatus(int userId, boolean isAdmin) {
        return userManagementService.toggleAdminStatus(userId, isAdmin);
    }
    
    // Category management (for dropdowns, etc.)
    
    /**
     * Get all categories
     * @return list of all categories
     */
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }
    
    /**
     * Get all main categories (not subcategories)
     * @return list of main categories
     */
    public List<Category> getMainCategories() {
        return categoryDAO.findMainCategories();
    }
    
    /**
     * Get category by ID
     * @param categoryId the category ID
     * @return optional containing the category if found
     */
    public Optional<Category> getCategoryById(int categoryId) {
        return categoryDAO.findById(categoryId);
    }
} 