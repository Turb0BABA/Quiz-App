package com.quizapp.dao;

import com.quizapp.model.QuizResult;
import com.quizapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizResultDAO {
    private static final String INSERT_RESULT = "INSERT INTO quiz_results (user_id, category_id, score, total_questions) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_USER = "SELECT r.*, c.name as category_name FROM quiz_results r JOIN categories c ON r.category_id = c.category_id WHERE r.user_id = ? ORDER BY r.completed_at DESC";
    private static final String SELECT_LEADERBOARD = "SELECT r.*, u.username, c.name as category_name FROM quiz_results r JOIN users u ON r.user_id = u.user_id JOIN categories c ON r.category_id = c.category_id WHERE r.category_id = ? ORDER BY r.score DESC, r.completed_at ASC LIMIT 10";
    private static final String SELECT_GLOBAL_LEADERBOARD = 
        "SELECT r.*, u.username, c.name as category_name " +
        "FROM quiz_results r " +
        "JOIN users u ON r.user_id = u.user_id " +
        "JOIN categories c ON r.category_id = c.category_id " +
        "ORDER BY r.score DESC, r.completed_at ASC LIMIT 20";

    public QuizResult create(QuizResult result) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_RESULT, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, result.getUserId());
            stmt.setInt(2, result.getCategoryId());
            stmt.setInt(3, result.getScore());
            stmt.setInt(4, result.getTotalQuestions());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating quiz result failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    result.setResultId(generatedKeys.getInt(1));
                    return result;
                } else {
                    throw new SQLException("Creating quiz result failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating quiz result", e);
        }
    }

    public List<QuizResult> findByUserId(int userId) {
        List<QuizResult> results = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                QuizResult result = new QuizResult();
                result.setResultId(rs.getInt("result_id"));
                result.setUserId(rs.getInt("user_id"));
                result.setCategoryId(rs.getInt("category_id"));
                result.setScore(rs.getInt("score"));
                result.setTotalQuestions(rs.getInt("total_questions"));
                result.setCompletedAt(rs.getTimestamp("completed_at"));
                result.setCategoryName(rs.getString("category_name"));
                results.add(result);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding quiz results by user", e);
        }
        return results;
    }

    public List<QuizResult> getLeaderboard(int categoryId) {
        List<QuizResult> leaderboard = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_LEADERBOARD)) {
            
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                QuizResult result = new QuizResult();
                result.setResultId(rs.getInt("result_id"));
                result.setUserId(rs.getInt("user_id"));
                result.setCategoryId(rs.getInt("category_id"));
                result.setScore(rs.getInt("score"));
                result.setTotalQuestions(rs.getInt("total_questions"));
                result.setCompletedAt(rs.getTimestamp("completed_at"));
                result.setUsername(rs.getString("username"));
                result.setCategoryName(rs.getString("category_name"));
                leaderboard.add(result);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting leaderboard", e);
        }
        return leaderboard;
    }

    /**
     * Gets a global leaderboard with top results across all categories
     * 
     * @return List of QuizResult objects from all categories
     */
    public List<QuizResult> getGlobalLeaderboard() {
        List<QuizResult> leaderboard = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_GLOBAL_LEADERBOARD)) {
            
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                QuizResult result = new QuizResult();
                result.setResultId(rs.getInt("result_id"));
                result.setUserId(rs.getInt("user_id"));
                result.setCategoryId(rs.getInt("category_id"));
                result.setScore(rs.getInt("score"));
                result.setTotalQuestions(rs.getInt("total_questions"));
                result.setCompletedAt(rs.getTimestamp("completed_at"));
                result.setUsername(rs.getString("username"));
                result.setCategoryName(rs.getString("category_name"));
                leaderboard.add(result);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting global leaderboard", e);
        }
        return leaderboard;
    }
} 