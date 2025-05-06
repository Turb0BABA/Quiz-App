package com.quizapp.admin.model;

/**
 * Represents a user's response to a question in a quiz attempt, used for analytics.
 */
public class QuestionResponse {
    private int responseId;
    private int attemptId;
    private int questionId;
    private int selectedAnswerId;
    private boolean isCorrect;
    private int timeTaken; // in seconds
    
    // Additional fields for UI display
    private String questionText;
    
    public QuestionResponse() {
    }
    
    public QuestionResponse(int attemptId, int questionId, int selectedAnswerId, boolean isCorrect, int timeTaken) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedAnswerId = selectedAnswerId;
        this.isCorrect = isCorrect;
        this.timeTaken = timeTaken;
    }

    public int getResponseId() {
        return responseId;
    }

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getSelectedAnswerId() {
        return selectedAnswerId;
    }

    public void setSelectedAnswerId(int selectedAnswerId) {
        this.selectedAnswerId = selectedAnswerId;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(int timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    @Override
    public String toString() {
        return "QuestionResponse{" +
                "responseId=" + responseId +
                ", attemptId=" + attemptId +
                ", questionId=" + questionId +
                ", selectedAnswerId=" + selectedAnswerId +
                ", isCorrect=" + isCorrect +
                ", timeTaken=" + timeTaken +
                '}';
    }
} 