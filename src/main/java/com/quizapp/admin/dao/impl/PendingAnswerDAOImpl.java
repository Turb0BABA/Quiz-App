package com.quizapp.admin.dao.impl;

import com.quizapp.admin.dao.PendingAnswerDAO;
import com.quizapp.admin.model.PendingAnswer;
import com.quizapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PendingAnswerDAOImpl implements PendingAnswerDAO {
    private static final Logger LOGGER = Logger.getLogger(PendingAnswerDAOImpl.class.getName());
    
    private static final String INSERT_PENDING_ANSWER = 
            "INSERT INTO pending_answers (pending_question_id, answer_text, is_correct) " +
            "VALUES (?, ?, ?)";
    
    private static final String INSERT_BATCH_PENDING_ANSWERS = 
            "INSERT INTO pending_answers (pending_question_id, answer_text, is_correct) " +
            "VALUES (?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
            "SELECT * FROM pending_answers WHERE pending_answer_id = ?";
    
    private static final String SELECT_BY_PENDING_QUESTION_ID = 
            "SELECT * FROM pending_answers WHERE pending_question_id = ?";
    
    private static final String UPDATE_PENDING_ANSWER = 
            "UPDATE pending_answers " +
            "SET answer_text = ?, is_correct = ? " +
            "WHERE pending_answer_id = ?";
    
    private static final String DELETE_PENDING_ANSWER = 
            "DELETE FROM pending_answers WHERE pending_answer_id = ?";
    
    private static final String DELETE_BY_PENDING_QUESTION_ID = 
            "DELETE FROM pending_answers WHERE pending_question_id = ?";

    @Override
    public PendingAnswer create(PendingAnswer answer) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PENDING_ANSWER, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, answer.getPendingQuestionId());
            stmt.setString(2, answer.getAnswerText());
            stmt.setBoolean(3, answer.isCorrect());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating pending answer failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int answerId = generatedKeys.getInt(1);
                    answer.setPendingAnswerId(answerId);
                    return answer;
                } else {
                    throw new SQLException("Creating pending answer failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating pending answer", e);
            throw new RuntimeException("Error creating pending answer", e);
        }
    }

    @Override
    public int createBatch(List<PendingAnswer> answers) {
        if (answers.isEmpty()) {
            return 0;
        }
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_BATCH_PENDING_ANSWERS)) {
            
            conn.setAutoCommit(false);
            
            try {
                int count = 0;
                for (PendingAnswer answer : answers) {
                    stmt.setInt(1, answer.getPendingQuestionId());
                    stmt.setString(2, answer.getAnswerText());
                    stmt.setBoolean(3, answer.isCorrect());
                    stmt.addBatch();
                    count++;
                }
                
                int[] results = stmt.executeBatch();
                conn.commit();
                
                return count;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating batch pending answers", e);
            throw new RuntimeException("Error creating batch pending answers", e);
        }
    }

    @Override
    public Optional<PendingAnswer> findById(int pendingAnswerId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, pendingAnswerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PendingAnswer answer = mapResultSetToPendingAnswer(rs);
                    return Optional.of(answer);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding pending answer by ID", e);
            throw new RuntimeException("Error finding pending answer by ID", e);
        }
    }

    @Override
    public List<PendingAnswer> findByPendingQuestionId(int pendingQuestionId) {
        List<PendingAnswer> answers = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_PENDING_QUESTION_ID)) {
            
            stmt.setInt(1, pendingQuestionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PendingAnswer answer = mapResultSetToPendingAnswer(rs);
                    answers.add(answer);
                }
            }
            return answers;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding pending answers by question ID", e);
            throw new RuntimeException("Error finding pending answers by question ID", e);
        }
    }

    @Override
    public boolean update(PendingAnswer answer) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PENDING_ANSWER)) {
            
            stmt.setString(1, answer.getAnswerText());
            stmt.setBoolean(2, answer.isCorrect());
            stmt.setInt(3, answer.getPendingAnswerId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating pending answer", e);
            throw new RuntimeException("Error updating pending answer", e);
        }
    }

    @Override
    public boolean delete(int pendingAnswerId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PENDING_ANSWER)) {
            
            stmt.setInt(1, pendingAnswerId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting pending answer", e);
            throw new RuntimeException("Error deleting pending answer", e);
        }
    }

    @Override
    public int deleteByPendingQuestionId(int pendingQuestionId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_PENDING_QUESTION_ID)) {
            
            stmt.setInt(1, pendingQuestionId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting pending answers by question ID", e);
            throw new RuntimeException("Error deleting pending answers by question ID", e);
        }
    }
    
    private PendingAnswer mapResultSetToPendingAnswer(ResultSet rs) throws SQLException {
        PendingAnswer answer = new PendingAnswer();
        answer.setPendingAnswerId(rs.getInt("pending_answer_id"));
        answer.setPendingQuestionId(rs.getInt("pending_question_id"));
        answer.setAnswerText(rs.getString("answer_text"));
        answer.setCorrect(rs.getBoolean("is_correct"));
        return answer;
    }
} 