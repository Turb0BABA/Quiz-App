package com.quizapp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event manager to handle application-wide events and notifications
 * Implements Observer pattern to notify various components about changes
 */
public class ApplicationEventManager {
    // Singleton instance
    private static ApplicationEventManager instance;
    
    // Event types
    public enum EventType {
        CATEGORY_UPDATED,
        USER_UPDATED,
        QUIZ_COMPLETED,
        THEME_CHANGED
    }
    
    // Event listeners mapped by event type
    private final Map<EventType, List<EventListener>> listeners = new HashMap<>();
    
    // Interface for event listeners
    public interface EventListener {
        void onEvent(EventType eventType, Object data);
    }
    
    // Private constructor for singleton pattern
    private ApplicationEventManager() {
        // Initialize listener lists for each event type
        for (EventType type : EventType.values()) {
            listeners.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized ApplicationEventManager getInstance() {
        if (instance == null) {
            instance = new ApplicationEventManager();
        }
        return instance;
    }
    
    /**
     * Add a listener for a specific event type
     */
    public void addListener(EventType eventType, EventListener listener) {
        listeners.get(eventType).add(listener);
    }
    
    /**
     * Remove a listener for a specific event type
     */
    public void removeListener(EventType eventType, EventListener listener) {
        listeners.get(eventType).remove(listener);
    }
    
    /**
     * Fire an event to all registered listeners
     */
    public void fireEvent(EventType eventType, Object data) {
        for (EventListener listener : listeners.get(eventType)) {
            listener.onEvent(eventType, data);
        }
    }
} 
 