package com.quizapp.model;

import java.sql.Timestamp;
import java.util.List;

/**
 * Represents a quiz question entity.
 */
public class Question {

    private int questionId;
    private int categoryId; // Foreign key reference
    private String questionText;
    private List<String> options;
    private int correctOptionIndex;
    private int difficulty;  // Numeric difficulty (1-5)
    private String difficultyLevel;  // String difficulty level
    private int points;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean isFlagged;  // New field for flagging
    private String flagReason;  // Optional reason for flagging

    // Default constructor
    public Question() {
        this.difficultyLevel = "MEDIUM";  // Default difficulty
        this.points = 10;                // Default points
    }

    // Constructor with all fields
    public Question(int questionId, int categoryId, String questionText, List<String> options, int correctOptionIndex) {
        this();
        this.questionId = questionId;
        this.categoryId = categoryId;
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }

    // Getters and Setters
    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public String getFlagReason() {
        return flagReason;
    }

    public void setFlagReason(String flagReason) {
        this.flagReason = flagReason;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
        
        // Update difficultyLevel based on the numeric difficulty
        switch (difficulty) {
            case 1:
                this.difficultyLevel = "EASY";
                break;
            case 2:
                this.difficultyLevel = "MEDIUM_EASY";
                break;
            case 3:
                this.difficultyLevel = "MEDIUM";
                break;
            case 4:
                this.difficultyLevel = "MEDIUM_HARD";
                break;
            case 5:
                this.difficultyLevel = "HARD";
                break;
            default:
                this.difficultyLevel = "MEDIUM";
                break;
        }
    }

    // Optional: toString(), equals(), hashCode() methods
    @Override
    public String toString() {
        return "Question{" +
               "questionId=" + questionId +
               ", categoryId=" + categoryId +
               ", questionText='" + questionText + '\'' +
               '}';
    }
}