package com.quizapp.admin.model;

/**
 * Represents an answer to a pending question that is awaiting moderation.
 */
public class PendingAnswer {
    private int pendingAnswerId;
    private int pendingQuestionId;
    private String answerText;
    private boolean isCorrect;
    
    public PendingAnswer() {
    }
    
    public PendingAnswer(String answerText, boolean isCorrect) {
        this.answerText = answerText;
        this.isCorrect = isCorrect;
    }

    public int getPendingAnswerId() {
        return pendingAnswerId;
    }

    public void setPendingAnswerId(int pendingAnswerId) {
        this.pendingAnswerId = pendingAnswerId;
    }

    public int getPendingQuestionId() {
        return pendingQuestionId;
    }

    public void setPendingQuestionId(int pendingQuestionId) {
        this.pendingQuestionId = pendingQuestionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
    
    @Override
    public String toString() {
        return "PendingAnswer{" +
                "pendingAnswerId=" + pendingAnswerId +
                ", pendingQuestionId=" + pendingQuestionId +
                ", answerText='" + answerText + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
} 