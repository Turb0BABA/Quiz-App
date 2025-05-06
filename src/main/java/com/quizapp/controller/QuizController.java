package com.quizapp.controller;

import com.quizapp.model.Answer;
import com.quizapp.model.Category;
import com.quizapp.model.Question;
import com.quizapp.service.QuizService;
import com.quizapp.dao.QuizResultDAO;
import com.quizapp.model.QuizResult;

import javax.swing.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller for quiz-related operations
 */
public class QuizController {
    private final QuizService quizService;
    private final int userId;
    private List<Question> questions;
    private List<Answer> currentAnswers;
    private int currentQuestionIndex;
    private int score;
    private Category category;
    private int quizTimeSeconds;
    private Timer timer;
    private int timeLeft;
    private Runnable onQuizEnd;
    private QuizResultDAO quizResultDAO;

    public QuizController(int userId) {
        this.quizService = new QuizService();
        this.userId = userId;
        this.quizResultDAO = new QuizResultDAO();
    }

    public void startQuiz(Category category, int questionCount, int quizTimeSeconds, Runnable onQuizEnd) {
        this.category = category;
        this.questions = quizService.getQuestionsForQuiz(category.getCategoryId(), questionCount);
        this.currentQuestionIndex = 0;
        this.score = 0;
        this.quizTimeSeconds = quizTimeSeconds;
        this.onQuizEnd = onQuizEnd;
        startTimer();
    }

    public Question getCurrentQuestion() {
        if (questions == null || questions.isEmpty()) return null;
        return questions.get(currentQuestionIndex);
    }

    public List<Answer> loadCurrentAnswers() {
        if (questions == null || questions.isEmpty()) return List.of();
        currentAnswers = quizService.getAnswersForQuestion(getCurrentQuestion().getQuestionId());
        return currentAnswers;
    }

    public void submitAnswer(int selectedIndex) {
        if (currentAnswers != null && quizService.isCorrectAnswer(currentAnswers, selectedIndex)) {
            score++;
        }
        currentQuestionIndex++;
    }

    public boolean hasNextQuestion() {
        return questions != null && currentQuestionIndex < questions.size();
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return questions != null ? questions.size() : 0;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timeLeft = quizTimeSeconds;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeLeft--;
                if (timeLeft <= 0) {
                    timer.cancel();
                    SwingUtilities.invokeLater(() -> onQuizEnd.run());
                }
            }
        }, 1000, 1000);
    }

    public void stopTimer() {
        if (timer != null) timer.cancel();
    }

    public void saveQuizResult() {
        QuizResult result = new QuizResult();
        result.setUserId(userId);
        result.setCategoryId(category.getCategoryId());
        result.setScore(score);
        result.setTotalQuestions(getTotalQuestions());
        quizResultDAO.create(result);
    }

    /**
     * Get all main categories with their subcategories
     */
    public List<Category> getAllCategoriesWithSubcategories() {
        return quizService.getAllCategoriesWithSubcategories();
    }

    /**
     * Get all categories that have questions
     */
    public List<Category> getCategoriesWithQuestions() {
        return quizService.getCategoriesWithQuestions();
    }

    /**
     * Get subcategories for a given parent category
     */
    public List<Category> getSubcategories(int parentId) {
        return quizService.getSubcategories(parentId);
    }

    /**
     * Get questions for a given category, limited to the requested count
     */
    public List<Question> getQuestionsForQuiz(int categoryId, int count) {
        return quizService.getQuestionsForQuiz(categoryId, count);
    }

    /**
     * Get questions for a given category with specified difficulty level
     */
    public List<Question> getQuestionsForQuiz(int categoryId, int count, String difficultyLevel) {
        return quizService.getQuestionsForQuiz(categoryId, count, difficultyLevel);
    }

    /**
     * Save quiz result
     */
    public QuizResult saveQuizResult(QuizResult result) {
        return quizService.saveQuizResult(result);
    }

    /**
     * Get the user ID associated with this controller
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Get a specific number of randomized questions from a category
     * @deprecated Use getQuestionsForQuiz instead
     */
    @Deprecated
    public List<Question> getRandomizedQuestions(Category category, int count) {
        return quizService.getQuestionsForQuiz(category.getCategoryId(), count);
    }

    /**
     * Flag a question for review
     */
    public void flagQuestion(Question question) {
        quizService.flagQuestion(question);
    }
} 