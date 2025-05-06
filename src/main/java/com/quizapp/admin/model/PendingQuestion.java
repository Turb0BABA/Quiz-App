package com.quizapp.admin.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user-submitted question pending approval by an administrator.
 */
public class PendingQuestion {
    private int pendingId;
    private int submitterId;
    private int categoryId;
    private String questionText;
    private int difficulty;
    private Timestamp submissionDate;
    private String status; // "pending", "approved", "rejected"
    private Integer reviewerId;
    private Timestamp reviewDate;
    private String rejectionReason;
    
    // Additional fields for UI display
    private String submitterUsername;
    private String categoryName;
    private String reviewerUsername;
    private List<PendingAnswer> answers;
    
    public PendingQuestion() {
        this.answers = new ArrayList<>();
        this.status = "pending";
        this.submissionDate = new Timestamp(System.currentTimeMillis());
    }

    public int getPendingId() {
        return pendingId;
    }

    public void setPendingId(int pendingId) {
        this.pendingId = pendingId;
    }

    public int getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(int submitterId) {
        this.submitterId = submitterId;
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

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public Timestamp getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Timestamp submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Timestamp getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Timestamp reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getSubmitterUsername() {
        return submitterUsername;
    }

    public void setSubmitterUsername(String submitterUsername) {
        this.submitterUsername = submitterUsername;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public void setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
    }

    public List<PendingAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<PendingAnswer> answers) {
        this.answers = answers;
    }
    
    public void addAnswer(PendingAnswer answer) {
        this.answers.add(answer);
    }
    
    @Override
    public String toString() {
        return "PendingQuestion{" +
                "pendingId=" + pendingId +
                ", submitterId=" + submitterId +
                ", categoryId=" + categoryId +
                ", questionText='" + questionText + '\'' +
                ", difficulty=" + difficulty +
                ", submissionDate=" + submissionDate +
                ", status='" + status + '\'' +
                '}';
    }
} 