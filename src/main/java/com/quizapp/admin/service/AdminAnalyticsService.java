package com.quizapp.admin.service;

import com.quizapp.admin.dao.QuizAttemptDAO;
import com.quizapp.admin.dao.QuestionResponseDAO;
import com.quizapp.admin.dao.impl.QuizAttemptDAOImpl;
import com.quizapp.admin.model.QuizAttempt;
import com.quizapp.model.Category;
import com.quizapp.model.Question;
import com.quizapp.dao.CategoryDAO;
import com.quizapp.dao.QuestionDAO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Service class for handling quiz analytics
 */
public class AdminAnalyticsService {
    private final QuizAttemptDAO quizAttemptDAO;
    private final QuestionResponseDAO questionResponseDAO;
    private final CategoryDAO categoryDAO;
    private final QuestionDAO questionDAO;
    
    public AdminAnalyticsService() {
        this.quizAttemptDAO = new QuizAttemptDAOImpl();
        // Initialize other DAOs
        this.categoryDAO = new CategoryDAO();
        this.questionDAO = new QuestionDAO();
        // We'll implement QuestionResponseDAO later
        this.questionResponseDAO = null;
    }
    
    /**
     * Get quiz completion counts by category
     * @return map of category name to completion count
     */
    public Map<String, Integer> getCompletionCountsByCategory() {
        Map<String, Integer> countsByCategory = new HashMap<>();
        List<Category> categories = categoryDAO.findAll();
        
        for (Category category : categories) {
            int count = quizAttemptDAO.getCompletionCountByCategory(category.getCategoryId());
            countsByCategory.put(category.getName(), count);
        }
        
        return countsByCategory;
    }
    
    /**
     * Get average scores by category
     * @return map of category name to average score
     */
    public Map<String, Double> getAverageScoresByCategory() {
        Map<String, Double> scoresByCategory = new HashMap<>();
        List<Category> categories = categoryDAO.findAll();
        
        for (Category category : categories) {
            double avgScore = quizAttemptDAO.getAverageScoreByCategory(category.getCategoryId());
            scoresByCategory.put(category.getName(), avgScore);
        }
        
        return scoresByCategory;
    }
    
    /**
     * Get quiz attempt counts by date for the past month
     * @return map of date to attempt count
     */
    public Map<LocalDate, Integer> getAttemptCountsForPastMonth() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        return quizAttemptDAO.getAttemptCountsByDate(start, end);
    }
    
    /**
     * Get top categories by completion count
     * @param limit maximum number of categories to return
     * @return list of category names
     */
    public List<Map.Entry<String, Integer>> getTopCategoriesByCompletions(int limit) {
        Map<String, Integer> countsByCategory = getCompletionCountsByCategory();
        
        return countsByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all quiz attempts
     * @return list of quiz attempts
     */
    public List<QuizAttempt> getAllAttempts() {
        List<QuizAttempt> allAttempts = new ArrayList<>();
        List<Category> categories = categoryDAO.findAll();
        
        for (Category category : categories) {
            allAttempts.addAll(quizAttemptDAO.findByCategoryId(category.getCategoryId()));
        }
        
        return allAttempts;
    }
    
    /**
     * Get quiz attempts for a specific user
     * @param userId the user ID
     * @return list of quiz attempts
     */
    public List<QuizAttempt> getAttemptsByUser(int userId) {
        return quizAttemptDAO.findByUserId(userId);
    }
    
    /**
     * Create pie chart for category completion counts
     * @return JFreeChart pie chart
     */
    public JFreeChart createCategoryCompletionPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Integer> countsByCategory = getCompletionCountsByCategory();
        
        // Only include top categories to avoid cluttering the chart
        countsByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> dataset.setValue(entry.getKey(), entry.getValue()));
        
        return ChartFactory.createPieChart(
                "Quiz Completions by Category",
                dataset,
                true,  // include legend
                true,  // tooltips
                false  // URLs
        );
    }
    
    /**
     * Create bar chart for average scores by category
     * @return JFreeChart bar chart
     */
    public JFreeChart createCategoryScoresBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> scoresByCategory = getAverageScoresByCategory();
        
        scoresByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> dataset.addValue(entry.getValue(), "Average Score (%)", entry.getKey()));
        
        return ChartFactory.createBarChart(
                "Average Scores by Category",
                "Category",
                "Average Score (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,  // include legend
                true,  // tooltips
                false  // URLs
        );
    }
    
    /**
     * Create line chart for quiz attempts over time
     * @return JFreeChart line chart
     */
    public JFreeChart createAttemptsOverTimeChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<LocalDate, Integer> attemptsByDate = getAttemptCountsForPastMonth();
        
        attemptsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> dataset.addValue(entry.getValue(), "Attempts", entry.getKey().toString()));
        
        return ChartFactory.createLineChart(
                "Quiz Attempts Over Time",
                "Date",
                "Number of Attempts",
                dataset,
                PlotOrientation.VERTICAL,
                true,  // include legend
                true,  // tooltips
                false  // URLs
        );
    }
    
    /**
     * Record a quiz attempt
     * @param attempt the quiz attempt to record
     * @return the generated attempt ID
     */
    public int recordQuizAttempt(QuizAttempt attempt) {
        return quizAttemptDAO.create(attempt);
    }
} 