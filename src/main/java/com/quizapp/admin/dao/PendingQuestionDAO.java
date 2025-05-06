package com.quizapp.admin.dao;

import com.quizapp.admin.model.PendingQuestion;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for pending questions
 */
public interface PendingQuestionDAO {
    /**
     * Creates a new pending question record
     * @param question the pending question to create
     * @return the generated pending question with ID
     */
    PendingQuestion create(PendingQuestion question);
    
    /**
     * Finds a pending question by ID
     * @param pendingId the pending question ID
     * @return optional containing the pending question if found
     */
    Optional<PendingQuestion> findById(int pendingId);
    
    /**
     * Finds all pending questions with a specific status
     * @param status the status to filter by ("pending", "approved", "rejected")
     * @return list of pending questions
     */
    List<PendingQuestion> findByStatus(String status);
    
    /**
     * Finds all pending questions submitted by a specific user
     * @param submitterId the submitter user ID
     * @return list of pending questions
     */
    List<PendingQuestion> findBySubmitter(int submitterId);
    
    /**
     * Updates the status and review information for a pending question
     * @param pendingId the pending question ID
     * @param status the new status
     * @param reviewerId the reviewer user ID
     * @param rejectionReason the reason for rejection (if rejected)
     * @return true if update succeeded
     */
    boolean updateStatus(int pendingId, String status, int reviewerId, String rejectionReason);
    
    /**
     * Deletes a pending question
     * @param pendingId the pending question ID
     * @return true if deletion succeeded
     */
    boolean delete(int pendingId);
    
    /**
     * Approves a pending question and converts it to a regular question
     * @param pendingId the pending question ID
     * @param reviewerId the reviewer user ID
     * @return the ID of the newly created question
     */
    int approveAndConvertToQuestion(int pendingId, int reviewerId);
} 