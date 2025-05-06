package com.quizapp.admin.model;

import java.sql.Timestamp;

/**
 * Represents a user's attempt at a quiz, used for analytics.
 */
public class QuizAttempt {
    private int attemptId;
    private int userId;
    private int quizId;
    private int categoryId;
    private double score;
    private double maxScore;
    private int completionTime; // in seconds
    private Timestamp attemptDate;
    
    // Additional fields for UI display
    private String username;
    private String categoryName;
    
    public QuizAttempt() {
    }
    
    public QuizAttempt(int userId, int quizId, int categoryId, double score, double maxScore, int completionTime) {
        this.userId = userId;
        this.quizId = quizId;
        this.categoryId = categoryId;
        this.score = score;
        this.maxScore = maxScore;
        this.completionTime = completionTime;
        this.attemptDate = new Timestamp(System.currentTimeMillis());
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public Timestamp getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(Timestamp attemptDate) {
        this.attemptDate = attemptDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    /**
     * Calculate completion percentage (score / maxScore)
     * @return completion percentage between 0 and 100
     */
    public double getCompletionPercentage() {
        if (maxScore == 0) return 0;
        return (score / maxScore) * 100;
    }
    
    @Override
    public String toString() {
        return "QuizAttempt{" +
                "attemptId=" + attemptId +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", score=" + score +
                ", maxScore=" + maxScore +
                ", completionTime=" + completionTime +
                ", attemptDate=" + attemptDate +
                '}';
    }
} 