package com.quizapp.model;

import java.sql.Timestamp;

/**
 * Represents a user entity.
 */
public class User {

    private int userId;
    private String username;
    private String password;
    private String passwordHash;
    private String email;
    private boolean isAdmin;
    private String fullName;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private String passwordResetToken;
    private Timestamp passwordResetExpiry;
    private Timestamp lastLoginDate;

    // Default constructor
    public User() {
        this.isActive = true; // Default to active
    }

    // Constructor with all fields
    public User(int userId, String username, String password, String email, boolean isAdmin) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;
        this.isActive = true; // Default to active
    }

    // Getters and Setters for all fields
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        // Store in both fields for backward compatibility
        // This ensures that getPassword() returns the hashed password as expected by BCrypt.checkpw()
        this.passwordHash = passwordHash;
        this.password = passwordHash; // Critical fix: ensure password field contains the hash
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public Timestamp getPasswordResetExpiry() {
        return passwordResetExpiry;
    }

    public void setPasswordResetExpiry(Timestamp passwordResetExpiry) {
        this.passwordResetExpiry = passwordResetExpiry;
    }

    public Timestamp getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Timestamp lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    // Optional: toString(), equals(), hashCode() methods
    @Override
    public String toString() {
        return "User{" +
               "userId=" + userId +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", isAdmin=" + isAdmin +
               ", isActive=" + isActive +
               ", createdAt=" + createdAt +
               ", lastLogin=" + lastLogin +
               '}';
    }
}