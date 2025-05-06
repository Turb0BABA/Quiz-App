package com.quizapp.admin.dao.impl;

import com.quizapp.admin.dao.PendingAnswerDAO;
import com.quizapp.admin.dao.PendingQuestionDAO;
import com.quizapp.admin.model.PendingAnswer;
import com.quizapp.admin.model.PendingQuestion;
import com.quizapp.dao.AnswerDao;
import com.quizapp.dao.CategoryDAO;
import com.quizapp.dao.QuestionDAO;
import com.quizapp.dao.UserDAO;
import com.quizapp.model.Answer;
import com.quizapp.model.Question;
import com.quizapp.model.User;
import com.quizapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PendingQuestionDAOImpl implements PendingQuestionDAO {
    private static final Logger LOGGER = Logger.getLogger(PendingQuestionDAOImpl.class.getName());
    
    private static final String INSERT_PENDING_QUESTION = 
            "INSERT INTO pending_questions (submitter_id, category_id, question_text, difficulty, " +
            "status, reviewer_id, review_date, rejection_reason) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
            "SELECT pq.*, u1.username as submitter_username, c.name as category_name, u2.username as reviewer_username " +
            "FROM pending_questions pq " +
            "JOIN users u1 ON pq.submitter_id = u1.user_id " +
            "JOIN categories c ON pq.category_id = c.category_id " +
            "LEFT JOIN users u2 ON pq.reviewer_id = u2.user_id " +
            "WHERE pq.pending_id = ?";
    
    private static final String SELECT_BY_STATUS = 
            "SELECT pq.*, u1.username as submitter_username, c.name as category_name, u2.username as reviewer_username " +
            "FROM pending_questions pq " +
            "JOIN users u1 ON pq.submitter_id = u1.user_id " +
            "JOIN categories c ON pq.category_id = c.category_id " +
            "LEFT JOIN users u2 ON pq.reviewer_id = u2.user_id " +
            "WHERE pq.status = ?";
    
    private static final String SELECT_BY_SUBMITTER = 
            "SELECT pq.*, u1.username as submitter_username, c.name as category_name, u2.username as reviewer_username " +
            "FROM pending_questions pq " +
            "JOIN users u1 ON pq.submitter_id = u1.user_id " +
            "JOIN categories c ON pq.category_id = c.category_id " +
            "LEFT JOIN users u2 ON pq.reviewer_id = u2.user_id " +
            "WHERE pq.submitter_id = ?";
    
    private static final String UPDATE_STATUS = 
            "UPDATE pending_questions " +
            "SET status = ?, reviewer_id = ?, review_date = ?, rejection_reason = ? " +
            "WHERE pending_id = ?";
    
    private static final String DELETE_PENDING_QUESTION = 
            "DELETE FROM pending_questions WHERE pending_id = ?";
    
    private final PendingAnswerDAO pendingAnswerDAO;
    private final QuestionDAO questionDAO;
    private final AnswerDao answerDao;
    private final UserDAO userDAO;
    private final CategoryDAO categoryDAO;
    
    public PendingQuestionDAOImpl() {
        this.pendingAnswerDAO = new PendingAnswerDAOImpl();
        this.questionDAO = new QuestionDAO();
        this.answerDao = new AnswerDao();
        this.userDAO = new UserDAO();
        this.categoryDAO = new CategoryDAO();
    }

    @Override
    public PendingQuestion create(PendingQuestion question) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PENDING_QUESTION, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, question.getSubmitterId());
            stmt.setInt(2, question.getCategoryId());
            stmt.setString(3, question.getQuestionText());
            stmt.setInt(4, question.getDifficulty());
            stmt.setString(5, question.getStatus());
            
            if (question.getReviewerId() != null) {
                stmt.setInt(6, question.getReviewerId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setTimestamp(7, question.getReviewDate());
            stmt.setString(8, question.getRejectionReason());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating pending question failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int pendingId = generatedKeys.getInt(1);
                    question.setPendingId(pendingId);
                    
                    // Create associated answers
                    for (PendingAnswer answer : question.getAnswers()) {
                        answer.setPendingQuestionId(pendingId);
                        pendingAnswerDAO.create(answer);
                    }
                    
                    return findById(pendingId).orElse(question);
                } else {
                    throw new SQLException("Creating pending question failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating pending question", e);
            throw new RuntimeException("Error creating pending question", e);
        }
    }

    @Override
    public Optional<PendingQuestion> findById(int pendingId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, pendingId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PendingQuestion question = mapResultSetToPendingQuestion(rs);
                    List<PendingAnswer> answers = pendingAnswerDAO.findByPendingQuestionId(pendingId);
                    question.setAnswers(answers);
                    return Optional.of(question);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding pending question by ID", e);
            throw new RuntimeException("Error finding pending question by ID", e);
        }
    }

    @Override
    public List<PendingQuestion> findByStatus(String status) {
        List<PendingQuestion> pendingQuestions = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_STATUS)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PendingQuestion question = mapResultSetToPendingQuestion(rs);
                    pendingQuestions.add(question);
                }
            }
            
            // Load answers for each question
            for (PendingQuestion question : pendingQuestions) {
                List<PendingAnswer> answers = pendingAnswerDAO.findByPendingQuestionId(question.getPendingId());
                question.setAnswers(answers);
            }
            
            return pendingQuestions;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding pending questions by status", e);
            throw new RuntimeException("Error finding pending questions by status", e);
        }
    }

    @Override
    public List<PendingQuestion> findBySubmitter(int submitterId) {
        List<PendingQuestion> pendingQuestions = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_SUBMITTER)) {
            
            stmt.setInt(1, submitterId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PendingQuestion question = mapResultSetToPendingQuestion(rs);
                    pendingQuestions.add(question);
                }
            }
            
            // Load answers for each question
            for (PendingQuestion question : pendingQuestions) {
                List<PendingAnswer> answers = pendingAnswerDAO.findByPendingQuestionId(question.getPendingId());
                question.setAnswers(answers);
            }
            
            return pendingQuestions;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding pending questions by submitter", e);
            throw new RuntimeException("Error finding pending questions by submitter", e);
        }
    }

    @Override
    public boolean updateStatus(int pendingId, String status, int reviewerId, String rejectionReason) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, reviewerId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, rejectionReason);
            stmt.setInt(5, pendingId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating pending question status", e);
            throw new RuntimeException("Error updating pending question status", e);
        }
    }

    @Override
    public boolean delete(int pendingId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PENDING_QUESTION)) {
            
            // First delete all associated answers
            pendingAnswerDAO.deleteByPendingQuestionId(pendingId);
            
            // Then delete the question
            stmt.setInt(1, pendingId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting pending question", e);
            throw new RuntimeException("Error deleting pending question", e);
        }
    }

    @Override
    public int approveAndConvertToQuestion(int pendingId, int reviewerId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Get the pending question with answers
                Optional<PendingQuestion> optPendingQuestion = findById(pendingId);
                if (!optPendingQuestion.isPresent()) {
                    throw new SQLException("Pending question not found with ID: " + pendingId);
                }
                
                PendingQuestion pendingQuestion = optPendingQuestion.get();
                List<PendingAnswer> pendingAnswers = pendingQuestion.getAnswers();
                
                if (pendingAnswers.isEmpty()) {
                    throw new SQLException("Pending question has no answers");
                }
                
                // Create the question
                Question question = new Question();
                question.setCategoryId(pendingQuestion.getCategoryId());
                question.setQuestionText(pendingQuestion.getQuestionText());
                question.setDifficulty(pendingQuestion.getDifficulty());
                
                Question createdQuestion = questionDAO.create(question);
                
                // Create the answers
                for (PendingAnswer pendingAnswer : pendingAnswers) {
                    Answer answer = new Answer();
                    answer.setQuestionId(createdQuestion.getQuestionId());
                    answer.setAnswerText(pendingAnswer.getAnswerText());
                    answer.setCorrect(pendingAnswer.isCorrect());
                    
                    answerDao.create(answer);
                }
                
                // Update the pending question status
                updateStatus(pendingId, "approved", reviewerId, null);
                
                conn.commit();
                return createdQuestion.getQuestionId();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error approving and converting pending question", e);
            throw new RuntimeException("Error approving and converting pending question", e);
        }
    }
    
    private PendingQuestion mapResultSetToPendingQuestion(ResultSet rs) throws SQLException {
        PendingQuestion question = new PendingQuestion();
        question.setPendingId(rs.getInt("pending_id"));
        question.setSubmitterId(rs.getInt("submitter_id"));
        question.setCategoryId(rs.getInt("category_id"));
        question.setQuestionText(rs.getString("question_text"));
        question.setDifficulty(rs.getInt("difficulty"));
        question.setSubmissionDate(rs.getTimestamp("submission_date"));
        question.setStatus(rs.getString("status"));
        
        // Handle nullable fields
        int reviewerId = rs.getInt("reviewer_id");
        if (!rs.wasNull()) {
            question.setReviewerId(reviewerId);
        }
        
        question.setReviewDate(rs.getTimestamp("review_date"));
        question.setRejectionReason(rs.getString("rejection_reason"));
        
        // Additional fields for UI display
        question.setSubmitterUsername(rs.getString("submitter_username"));
        question.setCategoryName(rs.getString("category_name"));
        question.setReviewerUsername(rs.getString("reviewer_username"));
        
        return question;
    }
} 