package com.quizapp.service;

import com.quizapp.dao.UserDAO;
import com.quizapp.model.User;
import com.quizapp.util.SessionManager;
import javax.swing.SwingUtilities;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Service for managing user sessions with improved persistence and security
 */
public class SessionService {
    private static SessionService instance;
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    private static final long TOKEN_REFRESH_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    
    private Timer sessionTimer;
    private Timer tokenRefreshTimer;
    private long lastActivityTime;
    private SessionTimeoutListener timeoutListener;
    private UserDAO userDAO;
    private User currentUser;
    private String currentToken;

    public interface SessionTimeoutListener {
        void onSessionTimeout();
    }

    private SessionService() {
        lastActivityTime = System.currentTimeMillis();
        userDAO = new UserDAO();
        startSessionTimer();
        startTokenRefreshTimer();
    }

    public static SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }

    public void setSessionTimeoutListener(SessionTimeoutListener listener) {
        this.timeoutListener = listener;
    }

    public void updateLastActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    private void startSessionTimer() {
        if (sessionTimer != null) {
            sessionTimer.cancel();
        }
        
        sessionTimer = new Timer(true);
        sessionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkSessionTimeout();
            }
        }, SESSION_TIMEOUT, SESSION_TIMEOUT);
    }
    
    private void startTokenRefreshTimer() {
        if (tokenRefreshTimer != null) {
            tokenRefreshTimer.cancel();
        }
        
        tokenRefreshTimer = new Timer(true);
        tokenRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isLoggedIn() && SessionManager.hasRememberedUser()) {
                    currentToken = SessionManager.refreshToken();
                }
            }
        }, TOKEN_REFRESH_INTERVAL, TOKEN_REFRESH_INTERVAL);
    }

    private void checkSessionTimeout() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActivityTime > SESSION_TIMEOUT) {
            if (timeoutListener != null) {
                SwingUtilities.invokeLater(() -> timeoutListener.onSessionTimeout());
            }
        }
    }

    /**
     * Login a user and optionally remember their session
     * 
     * @param username The username
     * @param password The password
     * @param rememberMe Whether to remember the user across application restarts
     * @return True if login successful
     */
    public boolean login(String username, String password, boolean rememberMe) {
        boolean authenticated = userDAO.authenticate(username, password);
        
        if (authenticated) {
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                userDAO.updateLastLogin(currentUser.getUserId());
                
                // Update AuthService
                AuthService.getInstance().login(username, password);
                
                // Store session with token
                if (rememberMe) {
                    currentToken = SessionManager.saveSession(username, password, true);
                } else {
                    // Keep in memory but don't persist
                    SessionManager.clearSession();
                }
                
                updateLastActivity();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Login a user without requiring password (for token-based authentication)
     * 
     * @param username The username
     * @return True if login successful
     */
    public boolean loginWithToken(String username) {
        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
            userDAO.updateLastLogin(currentUser.getUserId());
            
            // Update AuthService
            AuthService.getInstance().setCurrentUser(currentUser);
            
            updateLastActivity();
            return true;
        }
        return false;
    }

    /**
     * Logout the current user and clear session data
     */
    public void logout() {
        SessionManager.clearSession();
        AuthService.getInstance().logout();
        currentUser = null;
        currentToken = null;
        updateLastActivity();
    }

    /**
     * Check if there is a valid remembered session
     * 
     * @return True if there is a valid remembered session
     */
    public boolean hasValidSession() {
        return SessionManager.hasRememberedUser();
    }
    
    /**
     * Check if a user is currently logged in
     * 
     * @return True if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null || AuthService.getInstance().isLoggedIn();
    }
    
    /**
     * Get the current user
     * 
     * @return The current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Attempt to restore a session from persisted storage
     * 
     * @return True if session was restored successfully
     */
    public boolean restoreSession() {
        if (hasValidSession()) {
            String username = SessionManager.getRememberedUsername();
            if (username != null) {
                return loginWithToken(username);
            }
        }
        return false;
    }
} 