package com.quizapp.admin.dao;

import com.quizapp.admin.model.PendingAnswer;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for pending answers
 */
public interface PendingAnswerDAO {
    /**
     * Creates a new pending answer record
     * @param answer the pending answer to create
     * @return the generated pending answer with ID
     */
    PendingAnswer create(PendingAnswer answer);
    
    /**
     * Creates multiple pending answers in batch
     * @param answers list of pending answers to create
     * @return number of inserted records
     */
    int createBatch(List<PendingAnswer> answers);
    
    /**
     * Finds a pending answer by ID
     * @param pendingAnswerId the pending answer ID
     * @return optional containing the pending answer if found
     */
    Optional<PendingAnswer> findById(int pendingAnswerId);
    
    /**
     * Finds all pending answers for a specific pending question
     * @param pendingQuestionId the pending question ID
     * @return list of pending answers
     */
    List<PendingAnswer> findByPendingQuestionId(int pendingQuestionId);
    
    /**
     * Updates a pending answer
     * @param answer the pending answer to update
     * @return true if update succeeded
     */
    boolean update(PendingAnswer answer);
    
    /**
     * Deletes a pending answer
     * @param pendingAnswerId the pending answer ID
     * @return true if deletion succeeded
     */
    boolean delete(int pendingAnswerId);
    
    /**
     * Deletes all pending answers for a specific pending question
     * @param pendingQuestionId the pending question ID
     * @return number of deleted records
     */
    int deleteByPendingQuestionId(int pendingQuestionId);
} 