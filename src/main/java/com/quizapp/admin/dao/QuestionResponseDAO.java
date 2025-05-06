package com.quizapp.admin.dao;

import com.quizapp.admin.model.QuestionResponse;

import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for question responses
 */
public interface QuestionResponseDAO {
    /**
     * Creates a new question response record
     * @param response the response to create
     * @return the generated response ID
     */
    int create(QuestionResponse response);
    
    /**
     * Creates multiple question responses in batch
     * @param responses list of responses to create
     * @return number of inserted records
     */
    int createBatch(List<QuestionResponse> responses);
    
    /**
     * Finds all responses for a specific attempt
     * @param attemptId the attempt ID
     * @return list of responses
     */
    List<QuestionResponse> findByAttemptId(int attemptId);
    
    /**
     * Finds all responses for a specific question
     * @param questionId the question ID
     * @return list of responses
     */
    List<QuestionResponse> findByQuestionId(int questionId);
    
    /**
     * Gets percentage of correct answers for a specific question
     * @param questionId the question ID
     * @return percentage of correct answers (0-100)
     */
    double getCorrectAnswerPercentage(int questionId);
    
    /**
     * Gets average time taken for a specific question
     * @param questionId the question ID
     * @return average time in seconds
     */
    double getAverageTimeByQuestion(int questionId);
    
    /**
     * Gets count of correct/incorrect responses by question ID
     * @param questionIds list of question IDs
     * @return map of question ID to map with keys "correct" and "incorrect"
     */
    Map<Integer, Map<String, Integer>> getCorrectResponseCountsByQuestions(List<Integer> questionIds);
    
    /**
     * Gets the most incorrectly answered questions
     * @param limit maximum number of questions to return
     * @return list of question IDs sorted by incorrect answer rate (highest first)
     */
    List<Integer> getMostIncorrectlyAnsweredQuestions(int limit);
    
    /**
     * Gets the most correctly answered questions
     * @param limit maximum number of questions to return
     * @return list of question IDs sorted by correct answer rate (highest first)
     */
    List<Integer> getMostCorrectlyAnsweredQuestions(int limit);
} 