package com.quizapp.dao;

import com.quizapp.model.User;
import com.quizapp.util.DatabaseUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    private static final String SELECT_BY_USERNAME = "SELECT * FROM users WHERE username = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String SELECT_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String SELECT_ALL_USERS = "SELECT * FROM users ORDER BY user_id";
    private static final String INSERT_USER = "INSERT INTO users (username, password, email, is_admin, full_name) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_LAST_LOGIN = "UPDATE users SET last_login = ? WHERE user_id = ?";
    private static final String UPDATE_USER = "UPDATE users SET username = ?, email = ?, full_name = ? WHERE user_id = ?";
    private static final String UPDATE_USER_STATUS = "UPDATE users SET is_active = ? WHERE user_id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE user_id = ? AND username != 'admin'";
    private static final String UPDATE_PASSWORD = "UPDATE users SET password = ? WHERE user_id = ?";
    private static final String UPDATE_PASSWORD_RESET_TOKEN = "UPDATE users SET password_reset_token = ?, password_reset_expiry = ? WHERE user_id = ?";
    private static final String CLEAR_PASSWORD_RESET_TOKEN = "UPDATE users SET password_reset_token = NULL, password_reset_expiry = NULL WHERE user_id = ?";
    private static final String SELECT_BY_RESET_TOKEN = "SELECT * FROM users WHERE password_reset_token = ?";
    private static final String UPDATE_LAST_LOGIN_DATE = "UPDATE users SET last_login_date = ? WHERE user_id = ?";
    private static final String UPDATE_USER_ADMIN_STATUS = "UPDATE users SET is_admin = ? WHERE user_id = ?";

    public Optional<User> findByUsername(String username) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                String storedPassword = rs.getString("password");
                user.setPassword(storedPassword);
                user.setPasswordHash(storedPassword);
                user.setEmail(rs.getString("email"));
                user.setAdmin(rs.getBoolean("is_admin"));
                user.setFullName(rs.getString("full_name"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
            throw new RuntimeException("Error finding user by username", e);
        }
    }
    
    public Optional<User> findById(int userId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                String storedPassword = rs.getString("password");
                user.setPassword(storedPassword);
                user.setPasswordHash(storedPassword);
                user.setEmail(rs.getString("email"));
                user.setAdmin(rs.getBoolean("is_admin"));
                user.setFullName(rs.getString("full_name"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            throw new RuntimeException("Error finding user by ID", e);
        }
    }

    public boolean authenticate(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Special case for admin - simplified authentication
            if (username.equals("admin")) {
                // Direct comparison for admin password
                return password.equals(user.getPassword());
            }
            
            // Check if user is active before authenticating
            if (!user.isActive()) {
                System.out.println("User account is inactive: " + username);
                return false;
            }
            
            return BCrypt.checkpw(password, user.getPassword());
        }
        return false;
    }

    public User create(User user) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            
            String hashedPassword = user.getUsername().equals("admin") ? 
                user.getPassword() : 
                BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.getEmail());
            stmt.setBoolean(4, user.isAdmin());
            stmt.setString(5, user.getFullName());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            // Update the user object to maintain the hashed password
            user.setPassword(hashedPassword);
            user.setPasswordHash(hashedPassword);

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                    return user;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            throw new RuntimeException("Error creating user", e);
        }
    }
    
    public void update(User user) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFullName());
            stmt.setInt(4, user.getUserId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            throw new RuntimeException("Error updating user", e);
        }
    }

    public void updateLastLogin(int userId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LAST_LOGIN)) {
            
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            throw new RuntimeException("Error updating last login", e);
        }
    }
    
    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_USERS)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setAdmin(rs.getBoolean("is_admin"));
                user.setFullName(rs.getString("full_name"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            throw new RuntimeException("Error fetching all users", e);
        }
        return users;
    }
    
    public void updateUserStatus(int userId, boolean isActive) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER_STATUS)) {
            
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
            throw new RuntimeException("Error updating user status", e);
        }
    }
    
    public void deleteUser(int userId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_USER)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMAIL)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                String storedPassword = rs.getString("password");
                user.setPassword(storedPassword);
                user.setPasswordHash(storedPassword);
                user.setEmail(rs.getString("email"));
                user.setAdmin(rs.getBoolean("is_admin"));
                user.setFullName(rs.getString("full_name"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    /**
     * Update a user's password
     * @param userId the user ID
     * @param newPassword the new password (will be hashed)
     */
    public void updateUserPassword(int userId, String newPassword) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PASSWORD)) {
            
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user password: " + e.getMessage());
            throw new RuntimeException("Error updating user password", e);
        }
    }
    
    /**
     * Update a user's password reset token
     * @param userId the user ID
     * @param token the reset token
     * @param expiry the expiry timestamp
     */
    public void updatePasswordResetToken(int userId, String token, Timestamp expiry) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PASSWORD_RESET_TOKEN)) {
            
            stmt.setString(1, token);
            stmt.setTimestamp(2, expiry);
            stmt.setInt(3, userId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating password reset token: " + e.getMessage());
            throw new RuntimeException("Error updating password reset token", e);
        }
    }
    
    /**
     * Clear a user's password reset token
     * @param userId the user ID
     */
    public void clearPasswordResetToken(int userId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CLEAR_PASSWORD_RESET_TOKEN)) {
            
            stmt.setInt(1, userId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing password reset token: " + e.getMessage());
            throw new RuntimeException("Error clearing password reset token", e);
        }
    }
    
    /**
     * Find a user by password reset token
     * @param token the reset token
     * @return optional containing the user if found
     */
    public Optional<User> findByPasswordResetToken(String token) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_RESET_TOKEN)) {
            
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                String storedPassword = rs.getString("password");
                user.setPassword(storedPassword);
                user.setPasswordHash(storedPassword);
                user.setEmail(rs.getString("email"));
                user.setAdmin(rs.getBoolean("is_admin"));
                user.setFullName(rs.getString("full_name"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                user.setPasswordResetToken(rs.getString("password_reset_token"));
                user.setPasswordResetExpiry(rs.getTimestamp("password_reset_expiry"));
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Error finding user by reset token: " + e.getMessage());
            throw new RuntimeException("Error finding user by reset token", e);
        }
    }
    
    /**
     * Update a user's last login date
     * @param userId the user ID
     * @param timestamp the login timestamp
     */
    public void updateLastLoginDate(int userId, Timestamp timestamp) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LAST_LOGIN_DATE)) {
            
            stmt.setTimestamp(1, timestamp);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login date: " + e.getMessage());
            throw new RuntimeException("Error updating last login date", e);
        }
    }
    
    /**
     * Update a user's admin status
     * @param userId the user ID
     * @param isAdmin true to grant admin, false to revoke
     */
    public void updateUserAdminStatus(int userId, boolean isAdmin) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER_ADMIN_STATUS)) {
            
            stmt.setBoolean(1, isAdmin);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user admin status: " + e.getMessage());
            throw new RuntimeException("Error updating user admin status", e);
        }
    }
    
    /**
     * Update a user's information (all fields)
     * @param user the user to update
     */
    public void updateUser(User user) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE users SET username = ?, email = ?, full_name = ?, is_admin = ?, is_active = ? WHERE user_id = ?")) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFullName());
            stmt.setBoolean(4, user.isAdmin());
            stmt.setBoolean(5, user.isActive());
            stmt.setInt(6, user.getUserId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            throw new RuntimeException("Error updating user", e);
        }
    }
} 