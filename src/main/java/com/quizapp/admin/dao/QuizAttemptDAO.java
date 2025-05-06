package com.quizapp.admin.dao;

import com.quizapp.admin.model.QuizAttempt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for quiz attempts
 */
public interface QuizAttemptDAO {
    /**
     * Creates a new quiz attempt record
     * @param attempt the attempt to create
     * @return the generated attempt ID
     */
    int create(QuizAttempt attempt);
    
    /**
     * Finds all attempts by a specific user
     * @param userId the user ID
     * @return list of attempts
     */
    List<QuizAttempt> findByUserId(int userId);
    
    /**
     * Finds all attempts for a specific quiz
     * @param quizId the quiz ID
     * @return list of attempts
     */
    List<QuizAttempt> findByQuizId(int quizId);
    
    /**
     * Finds all attempts in a specific category
     * @param categoryId the category ID
     * @return list of attempts
     */
    List<QuizAttempt> findByCategoryId(int categoryId);
    
    /**
     * Finds all attempts within a date range
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return list of attempts
     */
    List<QuizAttempt> findByDateRange(LocalDate start, LocalDate end);
    
    /**
     * Gets count of attempts by date for a given date range
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return map of date to attempt count
     */
    Map<LocalDate, Integer> getAttemptCountsByDate(LocalDate start, LocalDate end);
    
    /**
     * Gets average score for a specific quiz
     * @param quizId the quiz ID
     * @return average score as percentage
     */
    double getAverageScoreByQuiz(int quizId);
    
    /**
     * Gets average score for a specific category
     * @param categoryId the category ID
     * @return average score as percentage
     */
    double getAverageScoreByCategory(int categoryId);
    
    /**
     * Gets count of completions for a specific quiz
     * @param quizId the quiz ID
     * @return count of completions
     */
    int getCompletionCountByQuiz(int quizId);
    
    /**
     * Gets count of completions for a specific category
     * @param categoryId the category ID
     * @return count of completions
     */
    int getCompletionCountByCategory(int categoryId);
} 