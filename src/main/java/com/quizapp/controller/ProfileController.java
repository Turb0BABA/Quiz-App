package com.quizapp.controller;

import com.quizapp.model.User;
import com.quizapp.model.QuizResult;
import com.quizapp.service.ProfileService;

import java.util.List;
import java.util.Optional;

public class ProfileController {
    private final ProfileService profileService;
    private final int userId;

    public ProfileController(int userId) {
        this.profileService = new ProfileService();
        this.userId = userId;
    }

    public User getUser() {
        Optional<User> userOpt = profileService.getUserById(userId);
        return userOpt.orElse(null);
    }

    public void updateUser(User user) {
        profileService.updateUser(user);
    }

    public List<QuizResult> getQuizHistory() {
        return profileService.getQuizHistory(userId);
    }
} 