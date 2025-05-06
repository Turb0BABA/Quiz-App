package com.quizapp.service;

import com.quizapp.dao.QuizResultDAO;
import com.quizapp.dao.UserDAO;
import com.quizapp.model.QuizResult;
import com.quizapp.model.User;

import java.util.List;
import java.util.Optional;

public class ProfileService {
    private final UserDAO userDAO;
    private final QuizResultDAO quizResultDAO;

    public ProfileService() {
        this.userDAO = new UserDAO();
        this.quizResultDAO = new QuizResultDAO();
    }

    public Optional<User> getUserById(int userId) {
        return userDAO.findById(userId);
    }

    public void updateUser(User user) {
        userDAO.update(user);
    }

    public List<QuizResult> getQuizHistory(int userId) {
        return quizResultDAO.findByUserId(userId);
    }
} 