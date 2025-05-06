package com.quizapp.dao;

import com.quizapp.model.Question;
import com.quizapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestionDAO {
    private static final String SELECT_BY_CATEGORY = "SELECT * FROM questions WHERE category_id = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM questions WHERE question_id = ?";
    private static final String INSERT_QUESTION = "INSERT INTO questions (category_id, question_text, options, correct_option_index) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUESTION = "UPDATE questions SET question_text = ?, options = ?, correct_option_index = ? WHERE question_id = ?";
    private static final String DELETE_QUESTION = "DELETE FROM questions WHERE question_id = ?";
    private static final String FIND_DUPLICATE_QUESTIONS = 
        "SELECT MIN(q1.question_id) as keep_id, q1.question_text, q1.category_id, COUNT(*) as count " +
        "FROM questions q1 " +
        "JOIN questions q2 ON q1.question_text = q2.question_text AND q1.category_id = q2.category_id " +
        "GROUP BY q1.question_text, q1.category_id " +
        "HAVING COUNT(*) > 1";
    
    private static final String FIND_DUPLICATE_QUESTION_IDS = 
        "SELECT q.question_id " +
        "FROM questions q " +
        "WHERE q.question_text = ? AND q.category_id = ? AND q.question_id <> ?";

    private static final String UPDATE_FLAG = 
        "UPDATE questions SET is_flagged = ?, flag_reason = ? WHERE question_id = ?";

    private static final String SELECT_ALL = "SELECT * FROM questions ORDER BY category_id, question_id";

    public List<Question> findByCategoryId(int categoryId) {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CATEGORY)) {
            
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Question question = new Question();
                question.setQuestionId(rs.getInt("question_id"));
                question.setCategoryId(rs.getInt("category_id"));
                question.setQuestionText(rs.getString("question_text"));
                question.setOptions(List.of(rs.getString("options").split("\\|")));
                question.setCorrectOptionIndex(rs.getInt("correct_option_index"));
                question.setCreatedAt(rs.getTimestamp("created_at"));
                question.setUpdatedAt(rs.getTimestamp("updated_at"));
                questions.add(question);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding questions by category", e);
        }
        return questions;
    }

    public Optional<Question> findById(int questionId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Question question = new Question();
                question.setQuestionId(rs.getInt("question_id"));
                question.setCategoryId(rs.getInt("category_id"));
                question.setQuestionText(rs.getString("question_text"));
                question.setOptions(List.of(rs.getString("options").split("\\|")));
                question.setCorrectOptionIndex(rs.getInt("correct_option_index"));
                question.setCreatedAt(rs.getTimestamp("created_at"));
                question.setUpdatedAt(rs.getTimestamp("updated_at"));
                return Optional.of(question);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding question by ID", e);
        }
    }

    public Question create(Question question) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_QUESTION, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, question.getCategoryId());
            stmt.setString(2, question.getQuestionText());
            stmt.setString(3, String.join("|", question.getOptions()));
            stmt.setInt(4, question.getCorrectOptionIndex());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating question failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    question.setQuestionId(generatedKeys.getInt(1));
                    return question;
                } else {
                    throw new SQLException("Creating question failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating question", e);
        }
    }

    public void update(Question question) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_QUESTION)) {
            
            stmt.setString(1, question.getQuestionText());
            stmt.setString(2, String.join("|", question.getOptions()));
            stmt.setInt(3, question.getCorrectOptionIndex());
            stmt.setInt(4, question.getQuestionId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating question", e);
        }
    }

    public void delete(int questionId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_QUESTION)) {
            
            stmt.setInt(1, questionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting question", e);
        }
    }

    /**
     * Removes duplicate questions from the database.
     * Duplicate questions are defined as questions with the same text in the same category.
     * For each set of duplicates, keeps one question and deletes the rest.
     * @return Number of questions deleted
     */
    public int cleanupDuplicateQuestions() {
        int deletedCount = 0;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement findDuplicatesStmt = conn.prepareStatement(FIND_DUPLICATE_QUESTIONS);
             ResultSet rs = findDuplicatesStmt.executeQuery()) {
            
            while (rs.next()) {
                int keepId = rs.getInt("keep_id");
                String questionText = rs.getString("question_text");
                int categoryId = rs.getInt("category_id");
                
                // Find all duplicate IDs to delete (all except the one to keep)
                try (PreparedStatement findDuplicateIdsStmt = conn.prepareStatement(FIND_DUPLICATE_QUESTION_IDS)) {
                    findDuplicateIdsStmt.setString(1, questionText);
                    findDuplicateIdsStmt.setInt(2, categoryId);
                    findDuplicateIdsStmt.setInt(3, keepId);
                    
                    ResultSet duplicateIds = findDuplicateIdsStmt.executeQuery();
                    
                    // Delete each duplicate
                    while (duplicateIds.next()) {
                        int duplicateId = duplicateIds.getInt("question_id");
                        delete(duplicateId);
                        deletedCount++;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error cleaning up duplicate questions", e);
        }
        
        return deletedCount;
    }

    /**
     * Update the flag status of a question
     */
    public void updateFlag(Question question) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_FLAG)) {
            
            stmt.setBoolean(1, question.isFlagged());
            stmt.setString(2, question.getFlagReason());
            stmt.setInt(3, question.getQuestionId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating question flag", e);
        }
    }

    public List<Question> findAll() {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Question question = new Question();
                question.setQuestionId(rs.getInt("question_id"));
                question.setCategoryId(rs.getInt("category_id"));
                question.setQuestionText(rs.getString("question_text"));
                question.setOptions(List.of(rs.getString("options").split("\\|")));
                question.setCorrectOptionIndex(rs.getInt("correct_option_index"));
                
                // Try to get difficulty from result set if it exists
                try {
                    int difficulty = rs.getInt("difficulty");
                    if (!rs.wasNull()) {
                        question.setDifficulty(difficulty);
                    } else {
                        question.setDifficulty(3); // Default to medium difficulty
                    }
                } catch (SQLException e) {
                    // If difficulty column doesn't exist, set a default
                    question.setDifficulty(3);
                }
                
                // Set flag status if fields exist
                try {
                    boolean isFlagged = rs.getBoolean("is_flagged");
                    if (!rs.wasNull()) {
                        question.setFlagged(isFlagged);
                    }
                } catch (SQLException e) {
                    // If flag column doesn't exist, ignore
                }
                
                try {
                    String flagReason = rs.getString("flag_reason");
                    question.setFlagReason(flagReason);
                } catch (SQLException e) {
                    // If flag_reason column doesn't exist, ignore
                }
                
                question.setCreatedAt(rs.getTimestamp("created_at"));
                question.setUpdatedAt(rs.getTimestamp("updated_at"));
                questions.add(question);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all questions", e);
        }
        return questions;
    }
} 