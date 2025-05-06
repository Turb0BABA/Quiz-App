package com.quizapp.view.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * A customizable loading indicator component that can be used to
 * show progress while operations are being performed.
 */
public class LoadingIndicator extends JPanel {
    
    // Constants
    public enum Type {
        CIRCLE_SPIN,
        PROGRESS_BAR,
        DOTS,
        PULSE
    }
    
    private Type type;
    private Color foregroundColor;
    private Color backgroundColor;
    private int size;
    private Timer animationTimer;
    private double progress;
    private double angle;
    private String message;
    private boolean indeterminate;
    private int animationStep;
    
    /**
     * Creates a new loading indicator with default settings
     */
    public LoadingIndicator() {
        this(Type.CIRCLE_SPIN, 50);
    }
    
    /**
     * Creates a new loading indicator with the specified type and size
     * 
     * @param type The type of loading indicator
     * @param size The size (diameter) of the indicator
     */
    public LoadingIndicator(Type type, int size) {
        this.type = type;
        this.size = size;
        this.foregroundColor = UIManager.getColor("ProgressBar.foreground");
        if (this.foregroundColor == null) {
            this.foregroundColor = new Color(0, 120, 215);
        }
        this.backgroundColor = new Color(foregroundColor.getRed(), 
                                        foregroundColor.getGreen(), 
                                        foregroundColor.getBlue(), 
                                        50);
        this.progress = 0.0;
        this.angle = 0.0;
        this.message = null;
        this.indeterminate = true;
        this.animationStep = 0;
        
        setOpaque(false);
        setPreferredSize(new Dimension(size, size));
        
        // Set up animation timer
        animationTimer = new Timer(16, e -> {
            updateAnimation();
            repaint();
        });
    }
    
    /**
     * Starts the loading animation
     */
    public void startAnimation() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }
    
    /**
     * Stops the loading animation
     */
    public void stopAnimation() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }
    
    /**
     * Updates the animation state
     */
    private void updateAnimation() {
        switch (type) {
            case CIRCLE_SPIN:
                angle = (angle + 5) % 360;
                break;
            case DOTS:
                animationStep = (animationStep + 1) % 8;
                break;
            case PULSE:
                animationStep = (animationStep + 1) % 20;
                break;
            case PROGRESS_BAR:
                if (indeterminate) {
                    angle = (angle + 5) % 360;
                }
                break;
        }
    }
    
    /**
     * Sets the progress value (0.0 to 1.0)
     * 
     * @param progress The progress value (0.0 to 1.0)
     */
    public void setProgress(double progress) {
        this.progress = Math.min(1.0, Math.max(0.0, progress));
        this.indeterminate = false;
        repaint();
    }
    
    /**
     * Sets whether the progress is indeterminate
     * 
     * @param indeterminate true for indeterminate progress
     */
    public void setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
        repaint();
    }
    
    /**
     * Sets the message to display with the loading indicator
     * 
     * @param message The message to display
     */
    public void setMessage(String message) {
        this.message = message;
        repaint();
    }
    
    /**
     * Sets the foreground color of the loading indicator
     * 
     * @param color The foreground color
     */
    public void setForegroundColor(Color color) {
        this.foregroundColor = color;
        this.backgroundColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 50);
        repaint();
    }
    
    /**
     * Sets the background color of the loading indicator
     * 
     * @param color The background color
     */
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }
    
    /**
     * Sets the type of loading indicator
     * 
     * @param type The type of loading indicator
     */
    public void setType(Type type) {
        this.type = type;
        repaint();
    }
    
    /**
     * Sets the size of the loading indicator
     * 
     * @param size The size (diameter) of the indicator
     */
    public void setIndicatorSize(int size) {
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        revalidate();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate center and radius
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(centerX, centerY) - 5;
        
        // Draw based on type
        switch (type) {
            case CIRCLE_SPIN:
                drawCircleSpin(g2d, centerX, centerY, radius);
                break;
            case PROGRESS_BAR:
                drawProgressBar(g2d, centerX, centerY, radius);
                break;
            case DOTS:
                drawDots(g2d, centerX, centerY, radius);
                break;
            case PULSE:
                drawPulse(g2d, centerX, centerY, radius);
                break;
        }
        
        // Draw message if present
        if (message != null && !message.isEmpty()) {
            g2d.setColor(foregroundColor);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(message);
            int textHeight = fm.getHeight();
            g2d.drawString(message, centerX - textWidth / 2, centerY + radius + textHeight + 5);
        }
        
        g2d.dispose();
    }
    
    /**
     * Draws a spinning circle loading indicator
     */
    private void drawCircleSpin(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Draw background circle
        g2d.setColor(backgroundColor);
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw spinning arc
        g2d.setColor(foregroundColor);
        g2d.setStroke(new BasicStroke(radius / 4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        AffineTransform oldTransform = g2d.getTransform();
        g2d.rotate(Math.toRadians(angle), centerX, centerY);
        
        int arcAngle = indeterminate ? 90 : (int) (progress * 360);
        g2d.drawArc(centerX - radius + radius/8, centerY - radius + radius/8, 
                   radius * 2 - radius/4, radius * 2 - radius/4, 
                   0, arcAngle);
        
        g2d.setTransform(oldTransform);
    }
    
    /**
     * Draws a progress bar loading indicator
     */
    private void drawProgressBar(Graphics2D g2d, int centerX, int centerY, int radius) {
        int barWidth = radius * 2;
        int barHeight = radius / 3;
        int barX = centerX - barWidth / 2;
        int barY = centerY - barHeight / 2;
        
        // Draw background
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, barHeight, barHeight);
        
        // Draw progress
        g2d.setColor(foregroundColor);
        if (indeterminate) {
            // Draw moving segment for indeterminate mode
            int segmentWidth = barWidth / 3;
            int position = (int) (barWidth * (1 + Math.sin(Math.toRadians(angle))) / 2) - segmentWidth / 2;
            g2d.fillRoundRect(barX + position, barY, segmentWidth, barHeight, barHeight, barHeight);
        } else {
            // Draw progress for determinate mode
            int progressWidth = (int) (barWidth * progress);
            g2d.fillRoundRect(barX, barY, progressWidth, barHeight, barHeight, barHeight);
        }
    }
    
    /**
     * Draws a dots loading indicator
     */
    private void drawDots(Graphics2D g2d, int centerX, int centerY, int radius) {
        int dotCount = 8;
        int dotRadius = radius / 8;
        
        for (int i = 0; i < dotCount; i++) {
            double angle = Math.toRadians(i * (360 / dotCount));
            int x = (int) (centerX + radius * 0.7 * Math.cos(angle));
            int y = (int) (centerY + radius * 0.7 * Math.sin(angle));
            
            int alpha;
            if (indeterminate) {
                // Animated dots
                alpha = (i == animationStep) ? 255 : 100;
            } else {
                // Progress-based dots
                alpha = i <= (progress * dotCount) ? 255 : 100;
            }
            
            g2d.setColor(new Color(foregroundColor.getRed(), foregroundColor.getGreen(), 
                                  foregroundColor.getBlue(), alpha));
            g2d.fillOval(x - dotRadius, y - dotRadius, dotRadius * 2, dotRadius * 2);
        }
    }
    
    /**
     * Draws a pulsing circle loading indicator
     */
    private void drawPulse(Graphics2D g2d, int centerX, int centerY, int radius) {
        if (indeterminate) {
            // Pulse animation
            float pulseRadius = radius * (0.5f + 0.5f * (float) Math.sin(animationStep * 0.3));
            float alpha = 100 + 155 * (float) Math.cos(animationStep * 0.3);
            
            g2d.setColor(new Color(foregroundColor.getRed(), foregroundColor.getGreen(), 
                                  foregroundColor.getBlue(), (int) alpha));
            g2d.fillOval(centerX - (int) pulseRadius, centerY - (int) pulseRadius, 
                        (int) pulseRadius * 2, (int) pulseRadius * 2);
        } else {
            // Progress-based pulse
            g2d.setColor(backgroundColor);
            g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            
            // Draw progress
            g2d.setColor(foregroundColor);
            int progressRadius = (int) (radius * progress);
            g2d.fillOval(centerX - progressRadius, centerY - progressRadius, 
                        progressRadius * 2, progressRadius * 2);
        }
    }
    
    /**
     * Creates and displays a modal loading dialog with simplified visuals
     * 
     * @param parent The parent component
     * @param message The message to display
     * @param task The task to execute in the background
     * @param onComplete Callback when task completes
     * @return The loading dialog
     */
    public static JDialog showLoadingDialog(Component parent, String message, Type type, 
                                            Runnable task, Consumer<Void> onComplete) {
        // Find the parent frame or dialog
        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, "Loading", false);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, "Loading", false);
        } else {
            dialog = new JDialog((Frame) null, "Loading");
        }
        
        // Simple loading panel with spinner and message
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Simple loading message
        JLabel loadingLabel = new JLabel(message);
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.BOLD));
        
        // Add simple progress indicator - just a JProgressBar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        panel.add(loadingLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(parent);
        
        // Run task in background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }
            
            @Override
            protected void done() {
                dialog.dispose();
                if (onComplete != null) {
                    onComplete.accept(null);
                }
            }
        };
        
        worker.execute();
        dialog.setVisible(true);
        
        return dialog;
    }
} 