package com.quizapp.util.animation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages UI animations and transitions.
 */
public class AnimationManager {
    
    private static final Map<Component, Timer> activeAnimations = new HashMap<>();
    
    /**
     * Fades in a component
     * 
     * @param component The component to fade in
     * @param durationMs The duration in milliseconds
     * @param callback Optional callback when animation completes
     */
    public static void fadeIn(JComponent component, int durationMs, Consumer<Void> callback) {
        if (component == null) return;
        
        // Stop any active animation on this component
        stopAnimation(component);
        
        // Set initial opacity
        component.setOpaque(false);
        component.setVisible(true);
        
        // Get or create opacity layer
        ComponentOpacityLayer opacityLayer = getOrCreateOpacityLayer(component);
        opacityLayer.setOpacity(0.0f);
        
        // Calculate animation steps
        final int steps = Math.min(durationMs / 16, 30); // Max 30 steps, 16ms per step (~60fps)
        final float opacityIncrement = 1.0f / steps;
        
        // Create and start animation timer
        Timer timer = new Timer(durationMs / steps, null);
        final int[] step = {0};
        
        timer.addActionListener(e -> {
            step[0]++;
            float opacity = Math.min(opacityIncrement * step[0], 1.0f);
            opacityLayer.setOpacity(opacity);
            component.repaint();
            
            if (step[0] >= steps) {
                timer.stop();
                activeAnimations.remove(component);
                if (callback != null) {
                    callback.accept(null);
                }
            }
        });
        
        activeAnimations.put(component, timer);
        timer.start();
    }
    
    /**
     * Fades out a component
     * 
     * @param component The component to fade out
     * @param durationMs The duration in milliseconds
     * @param callback Optional callback when animation completes
     */
    public static void fadeOut(JComponent component, int durationMs, Consumer<Void> callback) {
        if (component == null) return;
        
        // Stop any active animation on this component
        stopAnimation(component);
        
        // Get or create opacity layer
        ComponentOpacityLayer opacityLayer = getOrCreateOpacityLayer(component);
        opacityLayer.setOpacity(1.0f);
        
        // Calculate animation steps
        final int steps = Math.min(durationMs / 16, 30); // Max 30 steps, 16ms per step (~60fps)
        final float opacityDecrement = 1.0f / steps;
        
        // Create and start animation timer
        Timer timer = new Timer(durationMs / steps, null);
        final int[] step = {0};
        
        timer.addActionListener(e -> {
            step[0]++;
            float opacity = Math.max(1.0f - (opacityDecrement * step[0]), 0.0f);
            opacityLayer.setOpacity(opacity);
            component.repaint();
            
            if (step[0] >= steps) {
                timer.stop();
                component.setVisible(false);
                activeAnimations.remove(component);
                if (callback != null) {
                    callback.accept(null);
                }
            }
        });
        
        activeAnimations.put(component, timer);
        timer.start();
    }
    
    /**
     * Slides in a component
     * 
     * @param component The component to slide in
     * @param direction The direction from which to slide
     * @param durationMs The duration in milliseconds
     * @param callback Optional callback when animation completes
     */
    public static void slideIn(JComponent component, SlideDirection direction, int durationMs, Consumer<Void> callback) {
        if (component == null) return;
        
        // Stop any active animation on this component
        stopAnimation(component);
        
        // Store original position and size
        final Rectangle originalBounds = component.getBounds();
        final Rectangle targetBounds = new Rectangle(originalBounds);
        
        // Set initial position based on slide direction
        Rectangle startBounds = new Rectangle(originalBounds);
        switch (direction) {
            case FROM_LEFT:
                startBounds.x = -originalBounds.width;
                break;
            case FROM_RIGHT:
                startBounds.x = component.getParent().getWidth();
                break;
            case FROM_TOP:
                startBounds.y = -originalBounds.height;
                break;
            case FROM_BOTTOM:
                startBounds.y = component.getParent().getHeight();
                break;
        }
        
        component.setBounds(startBounds);
        component.setVisible(true);
        
        // Calculate animation steps
        final int steps = Math.min(durationMs / 16, 30); // Max 30 steps, 16ms per step (~60fps)
        
        // Create and start animation timer
        Timer timer = new Timer(durationMs / steps, null);
        final int[] step = {0};
        
        timer.addActionListener(e -> {
            step[0]++;
            float progress = (float) step[0] / steps;
            
            // Calculate current position with easing
            float easedProgress = easeInOut(progress);
            int x = startBounds.x + Math.round(easedProgress * (targetBounds.x - startBounds.x));
            int y = startBounds.y + Math.round(easedProgress * (targetBounds.y - startBounds.y));
            
            component.setBounds(x, y, originalBounds.width, originalBounds.height);
            
            if (step[0] >= steps) {
                timer.stop();
                component.setBounds(targetBounds);
                activeAnimations.remove(component);
                if (callback != null) {
                    callback.accept(null);
                }
            }
        });
        
        activeAnimations.put(component, timer);
        timer.start();
    }
    
    /**
     * Slides out a component
     * 
     * @param component The component to slide out
     * @param direction The direction to which to slide
     * @param durationMs The duration in milliseconds
     * @param callback Optional callback when animation completes
     */
    public static void slideOut(JComponent component, SlideDirection direction, int durationMs, Consumer<Void> callback) {
        if (component == null) return;
        
        // Stop any active animation on this component
        stopAnimation(component);
        
        // Store original position and size
        final Rectangle originalBounds = component.getBounds();
        
        // Set target position based on slide direction
        Rectangle targetBounds = new Rectangle(originalBounds);
        switch (direction) {
            case TO_LEFT:
                targetBounds.x = -originalBounds.width;
                break;
            case TO_RIGHT:
                targetBounds.x = component.getParent().getWidth();
                break;
            case TO_TOP:
                targetBounds.y = -originalBounds.height;
                break;
            case TO_BOTTOM:
                targetBounds.y = component.getParent().getHeight();
                break;
        }
        
        // Calculate animation steps
        final int steps = Math.min(durationMs / 16, 30); // Max 30 steps, 16ms per step (~60fps)
        
        // Create and start animation timer
        Timer timer = new Timer(durationMs / steps, null);
        final int[] step = {0};
        
        timer.addActionListener(e -> {
            step[0]++;
            float progress = (float) step[0] / steps;
            
            // Calculate current position with easing
            float easedProgress = easeInOut(progress);
            int x = originalBounds.x + Math.round(easedProgress * (targetBounds.x - originalBounds.x));
            int y = originalBounds.y + Math.round(easedProgress * (targetBounds.y - originalBounds.y));
            
            component.setBounds(x, y, originalBounds.width, originalBounds.height);
            
            if (step[0] >= steps) {
                timer.stop();
                component.setVisible(false);
                component.setBounds(originalBounds); // Restore original bounds
                activeAnimations.remove(component);
                if (callback != null) {
                    callback.accept(null);
                }
            }
        });
        
        activeAnimations.put(component, timer);
        timer.start();
    }
    
    /**
     * Creates a smooth transition between two panels in a CardLayout
     * 
     * @param container The container with CardLayout
     * @param cardLayout The CardLayout
     * @param currentComponent The current component/panel
     * @param nextComponent The next component/panel
     * @param direction The transition direction
     * @param durationMs The duration in milliseconds
     */
    public static void cardTransition(Container container, CardLayout cardLayout, 
                                       Component currentComponent, Component nextComponent, 
                                       SlideDirection direction, int durationMs) {
        if (container == null || cardLayout == null || currentComponent == null || nextComponent == null) return;
        
        // Create a glass pane to perform the animation
        JPanel glassPane = new JPanel(null);
        glassPane.setOpaque(false);
        
        // Take screenshots of current and next components
        BufferedImage currentImg = createComponentImage(currentComponent);
        
        // Show next component briefly to capture it
        nextComponent.setVisible(true);
        BufferedImage nextImg = createComponentImage(nextComponent);
        nextComponent.setVisible(false);
        
        // Create animated panels
        JPanel currentPanel = new ImagePanel(currentImg);
        JPanel nextPanel = new ImagePanel(nextImg);
        
        // Set initial positions
        currentPanel.setBounds(0, 0, container.getWidth(), container.getHeight());
        nextPanel.setBounds(getStartPosition(direction, container));
        
        // Add panels to glass pane
        glassPane.add(currentPanel);
        glassPane.add(nextPanel);
        
        // Add glass pane to the container
        Container topLevelContainer = getTopLevelContainer(container);
        if (topLevelContainer instanceof JFrame) {
            JFrame frame = (JFrame) topLevelContainer;
            JPanel originalGlassPane = (JPanel) frame.getGlassPane();
            frame.setGlassPane(glassPane);
            glassPane.setVisible(true);
            
            // Calculate animation steps
            final int steps = Math.min(durationMs / 16, 30); // Max 30 steps, 16ms per step (~60fps)
            
            // Create and start animation timer
            Timer timer = new Timer(durationMs / steps, null);
            final int[] step = {0};
            
            Rectangle currentStart = currentPanel.getBounds();
            Rectangle currentTarget = getEndPosition(getOppositeDirection(direction), container);
            
            Rectangle nextStart = nextPanel.getBounds();
            Rectangle nextTarget = new Rectangle(0, 0, container.getWidth(), container.getHeight());
            
            timer.addActionListener(e -> {
                step[0]++;
                float progress = (float) step[0] / steps;
                
                // Calculate positions with easing
                float easedProgress = easeInOut(progress);
                
                // Move current panel out
                int currentX = currentStart.x + Math.round(easedProgress * (currentTarget.x - currentStart.x));
                int currentY = currentStart.y + Math.round(easedProgress * (currentTarget.y - currentStart.y));
                currentPanel.setBounds(currentX, currentY, container.getWidth(), container.getHeight());
                
                // Move next panel in
                int nextX = nextStart.x + Math.round(easedProgress * (nextTarget.x - nextStart.x));
                int nextY = nextStart.y + Math.round(easedProgress * (nextTarget.y - nextStart.y));
                nextPanel.setBounds(nextX, nextY, container.getWidth(), container.getHeight());
                
                if (step[0] >= steps) {
                    timer.stop();
                    
                    // Show actual next component
                    cardLayout.show(container, getCardName(nextComponent, container));
                    
                    // Restore original glass pane
                    frame.setGlassPane(originalGlassPane);
                }
            });
            
            timer.start();
        }
    }
    
    /**
     * Creates a screenshot of a component
     * 
     * @param component The component to capture
     * @return A BufferedImage containing the component's rendering
     */
    private static BufferedImage createComponentImage(Component component) {
        BufferedImage image = new BufferedImage(
            component.getWidth(), 
            component.getHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        component.paint(image.getGraphics());
        return image;
    }
    
    /**
     * Gets the top level container (usually a JFrame) containing a component
     * 
     * @param component The component
     * @return The top level container
     */
    private static Container getTopLevelContainer(Component component) {
        if (component == null) return null;
        if (component instanceof JFrame) return (Container) component;
        
        return getTopLevelContainer(component.getParent());
    }
    
    /**
     * Gets the start position for a sliding animation based on direction
     * 
     * @param direction The slide direction
     * @param container The container
     * @return A Rectangle representing the start position and size
     */
    private static Rectangle getStartPosition(SlideDirection direction, Container container) {
        int width = container.getWidth();
        int height = container.getHeight();
        
        switch (direction) {
            case FROM_LEFT:
            case TO_LEFT:
                return new Rectangle(-width, 0, width, height);
            case FROM_RIGHT:
            case TO_RIGHT:
                return new Rectangle(width, 0, width, height);
            case FROM_TOP:
            case TO_TOP:
                return new Rectangle(0, -height, width, height);
            case FROM_BOTTOM:
            case TO_BOTTOM:
                return new Rectangle(0, height, width, height);
            default:
                return new Rectangle(0, 0, width, height);
        }
    }
    
    /**
     * Gets the end position for a sliding animation based on direction
     * 
     * @param direction The slide direction
     * @param container The container
     * @return A Rectangle representing the end position and size
     */
    private static Rectangle getEndPosition(SlideDirection direction, Container container) {
        int width = container.getWidth();
        int height = container.getHeight();
        
        switch (direction) {
            case FROM_LEFT:
            case TO_LEFT:
                return new Rectangle(-width, 0, width, height);
            case FROM_RIGHT:
            case TO_RIGHT:
                return new Rectangle(width, 0, width, height);
            case FROM_TOP:
            case TO_TOP:
                return new Rectangle(0, -height, width, height);
            case FROM_BOTTOM:
            case TO_BOTTOM:
                return new Rectangle(0, height, width, height);
            default:
                return new Rectangle(0, 0, width, height);
        }
    }
    
    /**
     * Gets the opposite direction for a slide
     * 
     * @param direction The original direction
     * @return The opposite direction
     */
    private static SlideDirection getOppositeDirection(SlideDirection direction) {
        switch (direction) {
            case FROM_LEFT: return SlideDirection.TO_LEFT;
            case FROM_RIGHT: return SlideDirection.TO_RIGHT;
            case FROM_TOP: return SlideDirection.TO_TOP;
            case FROM_BOTTOM: return SlideDirection.TO_BOTTOM;
            case TO_LEFT: return SlideDirection.FROM_LEFT;
            case TO_RIGHT: return SlideDirection.FROM_RIGHT;
            case TO_TOP: return SlideDirection.FROM_TOP;
            case TO_BOTTOM: return SlideDirection.FROM_BOTTOM;
            default: return SlideDirection.FROM_RIGHT;
        }
    }
    
    /**
     * Gets the card name for a component in a CardLayout
     * 
     * @param component The component to find
     * @param container The container with CardLayout
     * @return The card name or null if not found
     */
    private static String getCardName(Component component, Container container) {
        for (Component child : container.getComponents()) {
            if (child == component) {
                return component.getName();
            }
        }
        return null;
    }
    
    /**
     * Stops any active animation on a component
     * 
     * @param component The component
     */
    public static void stopAnimation(Component component) {
        Timer timer = activeAnimations.get(component);
        if (timer != null) {
            timer.stop();
            activeAnimations.remove(component);
        }
    }
    
    /**
     * Gets or creates an opacity layer for a component
     * 
     * @param component The component
     * @return The opacity layer
     */
    private static ComponentOpacityLayer getOrCreateOpacityLayer(JComponent component) {
        ComponentOpacityLayer opacityLayer = null;
        
        // Check if component already has an opacity layer
        for (int i = 0; i < component.getComponentCount(); i++) {
            Component child = component.getComponent(i);
            if (child instanceof ComponentOpacityLayer) {
                opacityLayer = (ComponentOpacityLayer) child;
                break;
            }
        }
        
        // Create new opacity layer if none exists
        if (opacityLayer == null) {
            opacityLayer = new ComponentOpacityLayer();
            component.add(opacityLayer);
            opacityLayer.setBounds(0, 0, component.getWidth(), component.getHeight());
        }
        
        return opacityLayer;
    }
    
    /**
     * Easing function for smooth animations
     * 
     * @param t Progress from 0.0 to 1.0
     * @return Eased value
     */
    private static float easeInOut(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }
    
    /**
     * A panel that displays an image
     */
    private static class ImagePanel extends JPanel {
        private final BufferedImage image;
        
        public ImagePanel(BufferedImage image) {
            this.image = image;
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, this);
            }
        }
    }
    
    /**
     * A transparent panel that controls opacity for a component
     */
    private static class ComponentOpacityLayer extends JPanel {
        private float opacity = 1.0f;
        
        public ComponentOpacityLayer() {
            setOpaque(false);
        }
        
        public void setOpacity(float opacity) {
            this.opacity = opacity;
        }
        
        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            super.paint(g2d);
            g2d.dispose();
        }
    }
} 