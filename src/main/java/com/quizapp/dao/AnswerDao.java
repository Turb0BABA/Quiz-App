package com.quizapp.dao;

import com.quizapp.model.Answer;
import com.quizapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerDao {
    private static final String SELECT_BY_QUESTION_ID = "SELECT answer_id, question_id, answer_text, is_correct, created_at FROM answers WHERE question_id = ?";
    private static final String INSERT_ANSWER = "INSERT INTO answers (question_id, answer_text, is_correct) VALUES (?, ?, ?)";

    public List<Answer> findByQuestionId(int questionId) {
        List<Answer> answers = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_QUESTION_ID)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Answer answer = new Answer();
                answer.setAnswerId(rs.getInt("answer_id"));
                answer.setQuestionId(rs.getInt("question_id"));
                answer.setAnswerText(rs.getString("answer_text"));
                answer.setCorrect(rs.getBoolean("is_correct"));
                answer.setCreatedAt(rs.getTimestamp("created_at"));
                answers.add(answer);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching answers for question", e);
        }
        return answers;
    }

    public Answer create(Answer answer) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ANSWER, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, answer.getQuestionId());
            stmt.setString(2, answer.getAnswerText());
            stmt.setBoolean(3, answer.isCorrect());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating answer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    answer.setAnswerId(generatedKeys.getInt(1));
                    return answer;
                } else {
                    throw new SQLException("Creating answer failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating answer", e);
        }
    }
} 