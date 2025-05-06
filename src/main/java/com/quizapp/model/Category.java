package com.quizapp.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a quiz category entity.
 */
public class Category {

    private int categoryId;
    private String name;
    private String description;
    private Integer parentId;
    private boolean isSubcategory;
    private int timePerQuestion;
    private int totalTime;
    private String iconPath;
    private int displayOrder;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Category> subcategories;
    private int questionCount;
    private int difficultyLevel;

    // Default constructor
    public Category() {
        this.timePerQuestion = 30;  // Default: 30 seconds per question
        this.totalTime = 600;       // Default: 10 minutes
        this.displayOrder = 0;
        this.isSubcategory = false;
        this.subcategories = new ArrayList<>();
        this.questionCount = 0;     // Default question count
        this.difficultyLevel = 1;   // Default difficulty (1=Beginner)
    }

    // Constructor with all fields
    public Category(int categoryId, String name, String description) {
        this();
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getParentCategoryId() {
        return parentId;
    }

    public boolean isSubcategory() {
        return isSubcategory;
    }

    public void setSubcategory(boolean subcategory) {
        isSubcategory = subcategory;
    }

    public int getTimePerQuestion() {
        return timePerQuestion;
    }

    public void setTimePerQuestion(int timePerQuestion) {
        this.timePerQuestion = timePerQuestion;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Category> getSubcategories() {
        return subcategories;
    }
    
    public void setSubcategories(List<Category> subcategories) {
        this.subcategories = subcategories;
    }
    
    public void addSubcategory(Category subcategory) {
        this.subcategories.add(subcategory);
    }
    
    public boolean hasSubcategories() {
        return !this.subcategories.isEmpty();
    }

    /**
     * Gets the number of questions in this category
     * @return the question count
     */
    public int getQuestionCount() {
        return questionCount;
    }

    /**
     * Sets the number of questions in this category
     * @param questionCount the question count
     */
    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    /**
     * Gets the difficulty level of this category
     * @return the difficulty level (1=Beginner, 2=Intermediate, 3=Advanced)
     */
    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    /**
     * Sets the difficulty level of this category
     * @param difficultyLevel the difficulty level
     */
    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    // Optional: toString(), equals(), hashCode() methods
    @Override
    public String toString() {
        // Often used for displaying in JComboBox or JList
        return name;
    }
}