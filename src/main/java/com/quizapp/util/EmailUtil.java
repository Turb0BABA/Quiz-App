package com.quizapp.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for sending emails.
 * This is a placeholder for a real email service.
 */
public class EmailUtil {
    private static final Logger LOGGER = Logger.getLogger(EmailUtil.class.getName());
    
    /**
     * Send a password reset email to a user.
     * @param email recipient email address
     * @param resetToken password reset token
     * @param username recipient username
     * @return true if the email was sent successfully
     */
    public static boolean sendPasswordResetEmail(String email, String resetToken, String username) {
        // This is a placeholder for a real email service
        // In a real application, you would use JavaMail or a third-party email service
        
        String subject = "Password Reset - Quiz Application";
        String message = "Hello " + username + ",\n\n"
                + "You have requested a password reset for your Quiz Application account.\n"
                + "Click the link below or copy and paste it into your browser to reset your password:\n\n"
                + "http://localhost:8080/reset-password?token=" + resetToken + "\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Regards,\nQuiz Application Team";
        
        // Log the email (for testing purposes)
        LOGGER.log(Level.INFO, "Password reset email would be sent to: " + email);
        LOGGER.log(Level.INFO, "Subject: " + subject);
        LOGGER.log(Level.INFO, "Message: " + message);
        
        return true;
    }
    
    /**
     * Send a notification email to a user.
     * @param email recipient email address
     * @param subject email subject
     * @param message email message
     * @return true if the email was sent successfully
     */
    public static boolean sendNotificationEmail(String email, String subject, String message) {
        // This is a placeholder for a real email service
        
        // Log the email (for testing purposes)
        LOGGER.log(Level.INFO, "Notification email would be sent to: " + email);
        LOGGER.log(Level.INFO, "Subject: " + subject);
        LOGGER.log(Level.INFO, "Message: " + message);
        
        return true;
    }
} 