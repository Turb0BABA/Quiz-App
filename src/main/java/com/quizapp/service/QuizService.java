package com.quizapp.service;

import com.quizapp.dao.AnswerDao;
import com.quizapp.dao.CategoryDAO;
import com.quizapp.dao.QuestionDAO;
import com.quizapp.dao.QuizResultDAO;
import com.quizapp.model.Answer;
import com.quizapp.model.Category;
import com.quizapp.model.Question;
import com.quizapp.model.QuizResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for quiz-related business logic.
 */
public class QuizService {
    private final CategoryDAO categoryDAO;
    private final QuestionDAO questionDAO;
    private final QuizResultDAO quizResultDAO;
    private final AnswerDao answerDao;

    public QuizService() {
        this.categoryDAO = new CategoryDAO();
        this.questionDAO = new QuestionDAO();
        this.quizResultDAO = new QuizResultDAO();
        this.answerDao = new AnswerDao();
    }
    
    /**
     * Get all main categories with their subcategories
     */
    public List<Category> getAllCategoriesWithSubcategories() {
        return categoryDAO.findAllWithSubcategories();
    }
    
    /**
     * Get all categories that have questions
     */
    public List<Category> getCategoriesWithQuestions() {
        return categoryDAO.findCategoriesWithQuestions();
    }
    
    /**
     * Get subcategories for a given parent category
     */
    public List<Category> getSubcategories(int parentId) {
        return categoryDAO.findSubcategories(parentId);
    }

    /**
     * Retrieves questions for a quiz, shuffled and limited to the requested count.
     */
    public List<Question> getQuestionsForQuiz(int categoryId, int count) {
        List<Question> allQuestions = questionDAO.findByCategoryId(categoryId);
        
        if (allQuestions.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Shuffle questions for random ordering
        Collections.shuffle(allQuestions);
        
        // Return all questions or limited count, whichever is less
        int actualCount = Math.min(count, allQuestions.size());
        return allQuestions.subList(0, actualCount);
    }
    
    /**
     * Retrieves questions for a quiz, with specific difficulty level if requested
     */
    public List<Question> getQuestionsForQuiz(int categoryId, int count, String difficultyLevel) {
        List<Question> allQuestions = questionDAO.findByCategoryId(categoryId);
        
        if (allQuestions.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Filter by difficulty if specified
        List<Question> filteredQuestions = allQuestions;
        if (difficultyLevel != null && !difficultyLevel.isEmpty()) {
            filteredQuestions = allQuestions.stream()
                .filter(q -> difficultyLevel.equalsIgnoreCase(q.getDifficultyLevel()))
                .collect(Collectors.toList());
            
            // If no questions match the difficulty, fall back to all questions
            if (filteredQuestions.isEmpty()) {
                filteredQuestions = allQuestions;
            }
        }
        
        // Shuffle questions for random ordering
        Collections.shuffle(filteredQuestions);
        
        // Return all questions or limited count, whichever is less
        int actualCount = Math.min(count, filteredQuestions.size());
        return filteredQuestions.subList(0, actualCount);
    }

    /**
     * Saves a quiz result and returns it with the generated ID.
     */
    public QuizResult saveQuizResult(QuizResult result) {
        return quizResultDAO.create(result);
    }

    public List<Answer> getAnswersForQuestion(int questionId) {
        try {
            // First try to get answers from the answers table
            List<Answer> answers = answerDao.findByQuestionId(questionId);
            if (!answers.isEmpty()) {
                return answers;
            }
        } catch (Exception e) {
            System.err.println("Error getting answers from answers table: " + e.getMessage());
        }
        
        // If no answers found or error occurred, create answers from question options
        return createAnswersFromQuestion(questionId);
    }

    private List<Answer> createAnswersFromQuestion(int questionId) {
        List<Answer> answers = new ArrayList<>();
        try {
            Question question = questionDAO.findById(questionId).orElse(null);
            if (question != null && question.getOptions() != null) {
                List<String> options = question.getOptions();
                int correctIndex = question.getCorrectOptionIndex();
                
                for (int i = 0; i < options.size(); i++) {
                    Answer answer = new Answer();
                    answer.setQuestionId(questionId);
                    answer.setAnswerId(i); // Temporary ID
                    answer.setAnswerText(options.get(i));
                    answer.setCorrect(i == correctIndex);
                    answers.add(answer);
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating answers from question: " + e.getMessage());
        }
        return answers;
    }

    public boolean isCorrectAnswer(List<Answer> answers, int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex >= answers.size()) return false;
        return answers.get(selectedIndex).isCorrect();
    }

    /**
     * Flag a question for review
     */
    public void flagQuestion(Question question) {
        questionDAO.updateFlag(question);
    }
} 