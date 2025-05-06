package com.quizapp.view.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Toast notification component for displaying non-intrusive feedback to the user.
 * Similar to Android's Toast notifications.
 */
public class Toast extends JPanel {
    
    // Types of toast with different colors
    public enum Type {
        INFO(new Color(50, 50, 50, 220), Color.WHITE),
        SUCCESS(new Color(46, 139, 87, 220), Color.WHITE),
        WARNING(new Color(255, 153, 0, 220), Color.WHITE),
        ERROR(new Color(178, 34, 34, 220), Color.WHITE);
        
        private final Color background;
        private final Color foreground;
        
        Type(Color background, Color foreground) {
            this.background = background;
            this.foreground = foreground;
        }
        
        public Color getBackground() {
            return background;
        }
        
        public Color getForeground() {
            return foreground;
        }
    }
    
    // Position of toast
    public enum Position {
        TOP, BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    }
    
    // Toast appearance constants
    private static final int PADDING = 15;
    private static final int CORNER_RADIUS = 15;
    private static final int MAX_WIDTH = 300;
    private static final int MIN_DISPLAY_TIME_MS = 2000; // Minimum display time
    private static final int DEFAULT_DISPLAY_TIME_MS = 3000; // Default display time
    
    // Queue of pending toasts
    private static final Queue<ToastRequest> pendingToasts = new LinkedList<>();
    private static boolean isShowingToast = false;
    
    // Current toast state
    private final String message;
    private final Type type;
    private final Position position;
    private final Timer dismissTimer;
    private final Component parent;
    private final int displayTimeMs;
    
    // Animation properties
    private float alpha = 0.0f;
    private Timer fadeTimer;
    private boolean isFadingIn = true;
    
    /**
     * Creates a new Toast notification
     * 
     * @param parent The parent component
     * @param message The message to display
     * @param type The type of toast
     * @param position The position to display the toast
     * @param displayTimeMs The time to display the toast in milliseconds
     */
    private Toast(Component parent, String message, Type type, Position position, int displayTimeMs) {
        this.parent = parent;
        this.message = message;
        this.type = type;
        this.position = position;
        this.displayTimeMs = Math.max(displayTimeMs, MIN_DISPLAY_TIME_MS);
        
        // Set up appearance
        setOpaque(false);
        setForeground(type.getForeground());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Allow clicking to dismiss
        
        // Set size based on message
        FontMetrics fm = getFontMetrics(getFont());
        int messageWidth = fm.stringWidth(message);
        int width = Math.min(MAX_WIDTH, messageWidth + PADDING * 2);
        int height = fm.getHeight() + PADDING * 2;
        
        setPreferredSize(new Dimension(width, height));
        
        // Set up dismissal timer
        dismissTimer = new Timer(displayTimeMs, e -> fadeOut());
        dismissTimer.setRepeats(false);
        
        // Handle click to dismiss
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dismissTimer.stop();
                fadeOut();
            }
        });
    }
    
    /**
     * Fade in the toast - simplified version with no animation
     */
    private void fadeIn() {
        alpha = 1.0f; // Set to fully visible immediately
        dismissTimer.start(); // Start dismiss timer right away
    }
    
    /**
     * Fade out the toast - simplified version with no animation
     */
    private void fadeOut() {
        dismissTimer.stop();
        destroyToast(); // Remove toast immediately
    }
    
    /**
     * Remove the toast from the display and show next toast if any
     */
    private void destroyToast() {
        // Get the glass pane
        JRootPane rootPane = SwingUtilities.getRootPane(parent);
        if (rootPane != null) {
            JPanel glassPane = (JPanel) rootPane.getGlassPane();
            glassPane.remove(this);
            glassPane.revalidate();
            glassPane.repaint();
            glassPane.setVisible(false);
        }
        
        // Mark as done showing
        isShowingToast = false;
        
        // Show next toast if any
        showNextToast();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Set transparency
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2d.setComposite(alphaComposite);
        
        // Draw rounded rectangle background
        g2d.setColor(type.getBackground());
        RoundRectangle2D.Float rect = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 
                                                              CORNER_RADIUS, CORNER_RADIUS);
        g2d.fill(rect);
        
        // Draw message
        g2d.setColor(getForeground());
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(message)) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(message, x, y);
        
        g2d.dispose();
    }
    
    /**
     * Show the toast notification
     */
    public void show() {
        // Get the glass pane of the window containing the parent component
        JRootPane rootPane = SwingUtilities.getRootPane(parent);
        if (rootPane == null) return;
        
        // Get or create glass pane
        JPanel glassPane = (JPanel) rootPane.getGlassPane();
        if (glassPane.getLayout() != null) {
            glassPane = new JPanel(null);
            rootPane.setGlassPane(glassPane);
        }
        
        // Add toast to glass pane
        glassPane.add(this);
        glassPane.setVisible(true);
        
        // Calculate position
        Dimension prefSize = getPreferredSize();
        int x = 0;
        int y = 0;
        
        switch (position) {
            case TOP:
                x = (rootPane.getWidth() - prefSize.width) / 2;
                y = 20;
                break;
            case BOTTOM:
                x = (rootPane.getWidth() - prefSize.width) / 2;
                y = rootPane.getHeight() - prefSize.height - 20;
                break;
            case TOP_LEFT:
                x = 20;
                y = 20;
                break;
            case TOP_RIGHT:
                x = rootPane.getWidth() - prefSize.width - 20;
                y = 20;
                break;
            case BOTTOM_LEFT:
                x = 20;
                y = rootPane.getHeight() - prefSize.height - 20;
                break;
            case BOTTOM_RIGHT:
                x = rootPane.getWidth() - prefSize.width - 20;
                y = rootPane.getHeight() - prefSize.height - 20;
                break;
            case CENTER:
                x = (rootPane.getWidth() - prefSize.width) / 2;
                y = (rootPane.getHeight() - prefSize.height) / 2;
                break;
        }
        
        // Set bounds
        setBounds(x, y, prefSize.width, prefSize.height);
        
        // Start fade in animation
        fadeIn();
    }
    
    /**
     * Show a toast notification if none is currently showing,
     * otherwise queue it up
     * 
     * @param parent The parent component
     * @param message The message to display
     * @param type The type of toast
     * @param position The position of the toast
     * @param displayTimeMs The time to display the toast in milliseconds
     */
    public static void makeText(Component parent, String message, Type type, Position position, int displayTimeMs) {
        ToastRequest request = new ToastRequest(parent, message, type, position, displayTimeMs);
        
        synchronized (pendingToasts) {
            pendingToasts.offer(request);
            
            if (!isShowingToast) {
                showNextToast();
            }
        }
    }
    
    /**
     * Show next toast in queue if any
     */
    private static void showNextToast() {
        synchronized (pendingToasts) {
            if (!pendingToasts.isEmpty()) {
                ToastRequest request = pendingToasts.poll();
                isShowingToast = true;
                
                SwingUtilities.invokeLater(() -> {
                    Toast toast = new Toast(request.parent, request.message, request.type, 
                                         request.position, request.displayTimeMs);
                    toast.show();
                });
            }
        }
    }
    
    /**
     * Show a toast notification with default settings (BOTTOM position, 3 seconds)
     * 
     * @param parent The parent component
     * @param message The message to display
     * @param type The type of toast
     */
    public static void makeText(Component parent, String message, Type type) {
        makeText(parent, message, type, Position.BOTTOM, DEFAULT_DISPLAY_TIME_MS);
    }
    
    /**
     * Show an info toast notification
     * 
     * @param parent The parent component
     * @param message The message to display
     */
    public static void info(Component parent, String message) {
        makeText(parent, message, Type.INFO);
    }
    
    /**
     * Show a success toast notification
     * 
     * @param parent The parent component
     * @param message The message to display
     */
    public static void success(Component parent, String message) {
        makeText(parent, message, Type.SUCCESS);
    }
    
    /**
     * Show a warning toast notification
     * 
     * @param parent The parent component
     * @param message The message to display
     */
    public static void warning(Component parent, String message) {
        makeText(parent, message, Type.WARNING);
    }
    
    /**
     * Show an error toast notification
     * 
     * @param parent The parent component
     * @param message The message to display
     */
    public static void error(Component parent, String message) {
        makeText(parent, message, Type.ERROR);
    }
    
    /**
     * Class to store toast request information
     */
    private static class ToastRequest {
        final Component parent;
        final String message;
        final Type type;
        final Position position;
        final int displayTimeMs;
        
        ToastRequest(Component parent, String message, Type type, Position position, int displayTimeMs) {
            this.parent = parent;
            this.message = message;
            this.type = type;
            this.position = position;
            this.displayTimeMs = displayTimeMs;
        }
    }
} 