package com.quizapp.admin.dao.impl;

import com.quizapp.admin.dao.QuizAttemptDAO;
import com.quizapp.admin.model.QuizAttempt;
import com.quizapp.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizAttemptDAOImpl implements QuizAttemptDAO {
    private static final Logger LOGGER = Logger.getLogger(QuizAttemptDAOImpl.class.getName());
    
    private static final String INSERT_ATTEMPT = 
            "INSERT INTO quiz_attempts (user_id, quiz_id, category_id, score, max_score, completion_time, attempt_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_USER_ID = 
            "SELECT a.*, u.username, c.name AS category_name " +
            "FROM quiz_attempts a " +
            "JOIN users u ON a.user_id = u.user_id " +
            "JOIN categories c ON a.category_id = c.category_id " +
            "WHERE a.user_id = ? " +
            "ORDER BY a.attempt_date DESC";
    
    private static final String SELECT_BY_QUIZ_ID = 
            "SELECT a.*, u.username, c.name AS category_name " +
            "FROM quiz_attempts a " +
            "JOIN users u ON a.user_id = u.user_id " +
            "JOIN categories c ON a.category_id = c.category_id " +
            "WHERE a.quiz_id = ? " +
            "ORDER BY a.attempt_date DESC";
    
    private static final String SELECT_BY_CATEGORY_ID = 
            "SELECT a.*, u.username, c.name AS category_name " +
            "FROM quiz_attempts a " +
            "JOIN users u ON a.user_id = u.user_id " +
            "JOIN categories c ON a.category_id = c.category_id " +
            "WHERE a.category_id = ? " +
            "ORDER BY a.attempt_date DESC";
    
    private static final String SELECT_BY_DATE_RANGE = 
            "SELECT a.*, u.username, c.name AS category_name " +
            "FROM quiz_attempts a " +
            "JOIN users u ON a.user_id = u.user_id " +
            "JOIN categories c ON a.category_id = c.category_id " +
            "WHERE DATE(a.attempt_date) BETWEEN ? AND ? " +
            "ORDER BY a.attempt_date DESC";
    
    private static final String COUNT_BY_DATE = 
            "SELECT DATE(attempt_date) as attempt_day, COUNT(*) as count " +
            "FROM quiz_attempts " +
            "WHERE DATE(attempt_date) BETWEEN ? AND ? " +
            "GROUP BY DATE(attempt_date) " +
            "ORDER BY attempt_day";
    
    private static final String AVG_SCORE_BY_QUIZ = 
            "SELECT AVG((score / max_score) * 100) as avg_score " +
            "FROM quiz_attempts " +
            "WHERE quiz_id = ?";
    
    private static final String AVG_SCORE_BY_CATEGORY = 
            "SELECT AVG((score / max_score) * 100) as avg_score " +
            "FROM quiz_attempts " +
            "WHERE category_id = ?";
    
    private static final String COUNT_COMPLETIONS_BY_QUIZ = 
            "SELECT COUNT(*) as completion_count " +
            "FROM quiz_attempts " +
            "WHERE quiz_id = ?";
    
    private static final String COUNT_COMPLETIONS_BY_CATEGORY = 
            "SELECT COUNT(*) as completion_count " +
            "FROM quiz_attempts " +
            "WHERE category_id = ?";

    @Override
    public int create(QuizAttempt attempt) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ATTEMPT, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, attempt.getUserId());
            stmt.setInt(2, attempt.getQuizId());
            stmt.setInt(3, attempt.getCategoryId());
            stmt.setDouble(4, attempt.getScore());
            stmt.setDouble(5, attempt.getMaxScore());
            stmt.setInt(6, attempt.getCompletionTime());
            
            if (attempt.getAttemptDate() != null) {
                stmt.setTimestamp(7, attempt.getAttemptDate());
            } else {
                stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating quiz attempt failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int attemptId = generatedKeys.getInt(1);
                    attempt.setAttemptId(attemptId);
                    return attemptId;
                } else {
                    throw new SQLException("Creating quiz attempt failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating quiz attempt", e);
            throw new RuntimeException("Error creating quiz attempt", e);
        }
    }

    @Override
    public List<QuizAttempt> findByUserId(int userId) {
        List<QuizAttempt> attempts = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER_ID)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSetToQuizAttempt(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding quiz attempts by user ID", e);
            throw new RuntimeException("Error finding quiz attempts by user ID", e);
        }
        return attempts;
    }

    @Override
    public List<QuizAttempt> findByQuizId(int quizId) {
        List<QuizAttempt> attempts = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_QUIZ_ID)) {
            
            stmt.setInt(1, quizId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSetToQuizAttempt(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding quiz attempts by quiz ID", e);
            throw new RuntimeException("Error finding quiz attempts by quiz ID", e);
        }
        return attempts;
    }

    @Override
    public List<QuizAttempt> findByCategoryId(int categoryId) {
        List<QuizAttempt> attempts = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CATEGORY_ID)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSetToQuizAttempt(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding quiz attempts by category ID", e);
            throw new RuntimeException("Error finding quiz attempts by category ID", e);
        }
        return attempts;
    }

    @Override
    public List<QuizAttempt> findByDateRange(LocalDate start, LocalDate end) {
        List<QuizAttempt> attempts = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_DATE_RANGE)) {
            
            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSetToQuizAttempt(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding quiz attempts by date range", e);
            throw new RuntimeException("Error finding quiz attempts by date range", e);
        }
        return attempts;
    }

    @Override
    public Map<LocalDate, Integer> getAttemptCountsByDate(LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> countsByDate = new HashMap<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_DATE)) {
            
            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("attempt_day").toLocalDate();
                    int count = rs.getInt("count");
                    countsByDate.put(date, count);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attempt counts by date", e);
            throw new RuntimeException("Error getting attempt counts by date", e);
        }
        return countsByDate;
    }

    @Override
    public double getAverageScoreByQuiz(int quizId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(AVG_SCORE_BY_QUIZ)) {
            
            stmt.setInt(1, quizId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_score");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting average score by quiz", e);
            throw new RuntimeException("Error getting average score by quiz", e);
        }
        return 0;
    }

    @Override
    public double getAverageScoreByCategory(int categoryId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(AVG_SCORE_BY_CATEGORY)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_score");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting average score by category", e);
            throw new RuntimeException("Error getting average score by category", e);
        }
        return 0;
    }

    @Override
    public int getCompletionCountByQuiz(int quizId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_COMPLETIONS_BY_QUIZ)) {
            
            stmt.setInt(1, quizId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("completion_count");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting completion count by quiz", e);
            throw new RuntimeException("Error getting completion count by quiz", e);
        }
        return 0;
    }

    @Override
    public int getCompletionCountByCategory(int categoryId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_COMPLETIONS_BY_CATEGORY)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("completion_count");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting completion count by category", e);
            throw new RuntimeException("Error getting completion count by category", e);
        }
        return 0;
    }
    
    private QuizAttempt mapResultSetToQuizAttempt(ResultSet rs) throws SQLException {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setAttemptId(rs.getInt("attempt_id"));
        attempt.setUserId(rs.getInt("user_id"));
        attempt.setQuizId(rs.getInt("quiz_id"));
        attempt.setCategoryId(rs.getInt("category_id"));
        attempt.setScore(rs.getDouble("score"));
        attempt.setMaxScore(rs.getDouble("max_score"));
        attempt.setCompletionTime(rs.getInt("completion_time"));
        attempt.setAttemptDate(rs.getTimestamp("attempt_date"));
        
        // Additional fields for UI display
        attempt.setUsername(rs.getString("username"));
        attempt.setCategoryName(rs.getString("category_name"));
        
        return attempt;
    }
} 