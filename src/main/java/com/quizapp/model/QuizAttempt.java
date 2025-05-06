package com.quizapp.model;

import java.sql.Timestamp;

/**
 * Represents a record of a completed quiz attempt by a user.
 */
public class QuizAttempt {

    private int attemptId;
    private int userId; // Foreign key reference
    private int categoryId; // Foreign key reference
    private int score;
    private int totalQuestions;
    private Timestamp attemptedAt;

    // Optional: Include User and Category objects or names if needed for display
    private String username; // Populated when fetching leaderboard data
    private String categoryName; // Populated when fetching leaderboard data


    // Default constructor
    public QuizAttempt() {
    }

    // Constructor with all fields
    public QuizAttempt(int attemptId, int userId, int categoryId, int score, int totalQuestions, Timestamp attemptedAt) {
        this.attemptId = attemptId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.attemptedAt = attemptedAt;
    }

    // Getters and Setters
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Timestamp getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(Timestamp attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    // Getters and setters for optional fields (username, categoryName)
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

    // Optional: toString(), equals(), hashCode() methods
    @Override
    public String toString() {
        return "QuizAttempt{" +
               "attemptId=" + attemptId +
               ", userId=" + userId +
               ", categoryId=" + categoryId +
               ", score=" + score +
               ", totalQuestions=" + totalQuestions +
               ", attemptedAt=" + attemptedAt +
               '}';
    }
}