package com.quizapp.dao;

import com.quizapp.model.Category;
import com.quizapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CategoryDAO {
    private static final String SELECT_ALL_CATEGORIES = 
        "SELECT category_id, name, description, parent_id, is_subcategory, time_per_question, " +
        "total_time, icon_path, display_order, created_at, updated_at " +
        "FROM categories ORDER BY display_order, name";
    
    private static final String SELECT_MAIN_CATEGORIES = 
        "SELECT category_id, name, description, parent_id, is_subcategory, time_per_question, " +
        "total_time, icon_path, display_order, created_at, updated_at " +
        "FROM categories WHERE is_subcategory = FALSE ORDER BY display_order, name";
    
    private static final String SELECT_SUBCATEGORIES = 
        "SELECT category_id, name, description, parent_id, is_subcategory, time_per_question, " +
        "total_time, icon_path, display_order, created_at, updated_at " +
        "FROM categories WHERE parent_id = ? ORDER BY display_order, name";
    
    private static final String SELECT_CATEGORY_BY_ID = 
        "SELECT category_id, name, description, parent_id, is_subcategory, time_per_question, " +
        "total_time, icon_path, display_order, created_at, updated_at " +
        "FROM categories WHERE category_id = ?";
    
    private static final String INSERT_CATEGORY = 
        "INSERT INTO categories (name, description, parent_id, is_subcategory, time_per_question, " +
        "total_time, icon_path, display_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_CATEGORY = 
        "UPDATE categories SET name = ?, description = ?, parent_id = ?, is_subcategory = ?, " +
        "time_per_question = ?, total_time = ?, icon_path = ?, display_order = ? " +
        "WHERE category_id = ?";
    
    private static final String DELETE_CATEGORY = "DELETE FROM categories WHERE category_id = ?";

    private static final String SELECT_CATEGORIES_WITH_QUESTIONS = 
        "SELECT c.category_id, c.name, c.description, c.parent_id, c.is_subcategory, " +
        "c.time_per_question, c.total_time, c.icon_path, c.display_order, c.created_at, c.updated_at, " +
        "COUNT(q.question_id) as question_count " +
        "FROM categories c " +
        "JOIN questions q ON c.category_id = q.category_id " +
        "GROUP BY c.category_id " +
        "HAVING question_count > 0 " +
        "ORDER BY c.display_order, c.name";
    
    private static final String FIND_DUPLICATE_CATEGORIES = 
        "SELECT c.name, COUNT(c.category_id) as category_count " +
        "FROM categories c " +
        "GROUP BY c.name " +
        "HAVING COUNT(c.category_id) > 1";
    
    private static final String FIND_CATEGORY_DETAILS_BY_NAME = 
        "SELECT c.category_id, c.name, c.description, c.parent_id, c.is_subcategory, " +
        "c.time_per_question, c.total_time, c.icon_path, c.display_order, c.created_at, c.updated_at, " +
        "COUNT(q.question_id) as question_count " +
        "FROM categories c " +
        "LEFT JOIN questions q ON c.category_id = q.category_id " +
        "WHERE c.name = ? " +
        "GROUP BY c.category_id " +
        "ORDER BY question_count DESC";
    
    /**
     * Returns all categories that have at least one question associated with them.
     * This helps filter out empty categories in the UI.
     */
    public List<Category> findCategoriesWithQuestions() {
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_CATEGORIES_WITH_QUESTIONS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                try {
                    categories.add(mapResultSetToCategory(rs));
                } catch (SQLException e) {
                    System.err.println("Error mapping category with questions: " + e.getMessage());
                    // Continue with next category
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in findCategoriesWithQuestions: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            // Return empty list instead of throwing exception
        }
        return categories;
    }

    /**
     * Returns all categories, including main categories and subcategories
     */
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_CATEGORIES);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                try {
                    categories.add(mapResultSetToCategory(rs));
                } catch (SQLException e) {
                    System.err.println("Error mapping category in findAll: " + e.getMessage());
                    // Continue with next category
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in findAll: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            // Return partially loaded list instead of throwing exception
        }
        return categories;
    }
    
    /**
     * Returns all main categories with their subcategories loaded
     */
    public List<Category> findAllWithSubcategories() {
        Map<Integer, Category> categoryMap = new HashMap<>();
        List<Category> mainCategories = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // First, get all main categories
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_MAIN_CATEGORIES);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    try {
                        Category category = mapResultSetToCategory(rs);
                        categoryMap.put(category.getCategoryId(), category);
                        mainCategories.add(category);
                    } catch (SQLException e) {
                        System.err.println("Error mapping main category: " + e.getMessage());
                        // Continue with next category
                    }
                }
            }
            
            // Then, for each main category, get its subcategories
            for (Category mainCategory : mainCategories) {
                try (PreparedStatement stmt = conn.prepareStatement(SELECT_SUBCATEGORIES)) {
                    stmt.setInt(1, mainCategory.getCategoryId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            try {
                                Category subcategory = mapResultSetToCategory(rs);
                                mainCategory.addSubcategory(subcategory);
                            } catch (SQLException e) {
                                System.err.println("Error mapping subcategory for category ID " + 
                                                  mainCategory.getCategoryId() + ": " + e.getMessage());
                                // Continue with next subcategory
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error loading subcategories for category ID " + 
                                      mainCategory.getCategoryId() + ": " + e.getMessage());
                    // Continue with the next category rather than failing the whole operation
                }
            }
            
            return mainCategories;
        } catch (SQLException e) {
            System.err.println("Database error in findAllWithSubcategories: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            // Return empty list instead of throwing exception to make application more robust
            return new ArrayList<>();
        }
    }
    
    /**
     * Returns all subcategories for a given parent category
     */
    public List<Category> findSubcategories(int parentId) {
        List<Category> subcategories = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_SUBCATEGORIES)) {
            
            stmt.setInt(1, parentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        subcategories.add(mapResultSetToCategory(rs));
                    } catch (SQLException e) {
                        System.err.println("Error mapping subcategory: " + e.getMessage());
                        // Continue with next subcategory
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in findSubcategories for parentId=" + parentId + ": " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            // Return empty list instead of throwing exception
        }
        
        return subcategories;
    }

    public Optional<Category> findById(int categoryId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_CATEGORY_BY_ID)) {
            
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                try {
                    return Optional.of(mapResultSetToCategory(rs));
                } catch (SQLException e) {
                    System.err.println("Error mapping category by ID " + categoryId + ": " + e.getMessage());
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Database error in findById for categoryId=" + categoryId + ": " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Category create(Category category) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_CATEGORY, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            
            if (category.getParentId() != null) {
                stmt.setInt(3, category.getParentId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setBoolean(4, category.isSubcategory());
            stmt.setInt(5, category.getTimePerQuestion());
            stmt.setInt(6, category.getTotalTime());
            
            if (category.getIconPath() != null) {
                stmt.setString(7, category.getIconPath());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }
            
            stmt.setInt(8, category.getDisplayOrder());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setCategoryId(generatedKeys.getInt(1));
                    return category;
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating category", e);
        }
    }

    public void update(Category category) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_CATEGORY)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            
            if (category.getParentId() != null) {
                stmt.setInt(3, category.getParentId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setBoolean(4, category.isSubcategory());
            stmt.setInt(5, category.getTimePerQuestion());
            stmt.setInt(6, category.getTotalTime());
            
            if (category.getIconPath() != null) {
                stmt.setString(7, category.getIconPath());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }
            
            stmt.setInt(8, category.getDisplayOrder());
            stmt.setInt(9, category.getCategoryId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating category", e);
        }
    }

    public void delete(int categoryId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_CATEGORY)) {
            
            stmt.setInt(1, categoryId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting category", e);
        }
    }

    /**
     * Cleans up duplicate categories in the database.
     * For each set of categories with the same name, keeps only the one with the most questions.
     * @return Number of categories deleted
     */
    public int cleanupDuplicateCategories() {
        int deletedCount = 0;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement findDuplicatesStmt = conn.prepareStatement(FIND_DUPLICATE_CATEGORIES)) {
            
            ResultSet duplicateResults = findDuplicatesStmt.executeQuery();
            
            while (duplicateResults.next()) {
                String categoryName = duplicateResults.getString("name");
                
                // Find all categories with this name
                try (PreparedStatement findDetailsStmt = conn.prepareStatement(FIND_CATEGORY_DETAILS_BY_NAME)) {
                    findDetailsStmt.setString(1, categoryName);
                    ResultSet detailsResults = findDetailsStmt.executeQuery();
                    
                    // The first result has the most questions (due to ORDER BY)
                    if (detailsResults.next()) {
                        int keepId = detailsResults.getInt("category_id");
                        
                        // Delete all other categories with the same name
                        while (detailsResults.next()) {
                            int deleteId = detailsResults.getInt("category_id");
                            delete(deleteId);
                            deletedCount++;
                        }
                    }
                }
            }
            
            return deletedCount;
        } catch (SQLException e) {
            throw new RuntimeException("Error cleaning up duplicate categories", e);
        }
    }
    
    /**
     * Helper method to map a ResultSet row to a Category object
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        try {
            Category category = new Category();
            category.setCategoryId(rs.getInt("category_id"));
            category.setName(rs.getString("name"));
            category.setDescription(rs.getString("description"));
            
            // Handle parent_id which might be NULL
            try {
                int parentId = rs.getInt("parent_id");
                if (!rs.wasNull()) {
                    category.setParentId(parentId);
                }
            } catch (SQLException e) {
                // Log but don't throw - parent_id might be optional in some contexts
                System.err.println("Error reading parent_id: " + e.getMessage());
            }
            
            // Handle boolean conversion safely
            try {
                category.setSubcategory(rs.getBoolean("is_subcategory"));
            } catch (SQLException e) {
                System.err.println("Error reading is_subcategory: " + e.getMessage());
                // Default to false if there's a problem
                category.setSubcategory(false);
            }
            
            // Set time-related fields with defaults if there's an issue
            try {
                category.setTimePerQuestion(rs.getInt("time_per_question"));
                if (rs.wasNull()) {
                    category.setTimePerQuestion(30); // Default value
                }
            } catch (SQLException e) {
                System.err.println("Error reading time_per_question: " + e.getMessage());
                category.setTimePerQuestion(30); // Default value
            }
            
            try {
                category.setTotalTime(rs.getInt("total_time"));
                if (rs.wasNull()) {
                    category.setTotalTime(600); // Default value
                }
            } catch (SQLException e) {
                System.err.println("Error reading total_time: " + e.getMessage());
                category.setTotalTime(600); // Default value
            }
            
            try {
                category.setIconPath(rs.getString("icon_path"));
            } catch (SQLException e) {
                System.err.println("Error reading icon_path: " + e.getMessage());
            }
            
            try {
                category.setDisplayOrder(rs.getInt("display_order"));
                if (rs.wasNull()) {
                    category.setDisplayOrder(0); // Default value
                }
            } catch (SQLException e) {
                System.err.println("Error reading display_order: " + e.getMessage());
                category.setDisplayOrder(0); // Default value
            }
            
            try {
                category.setCreatedAt(rs.getTimestamp("created_at"));
            } catch (SQLException e) {
                System.err.println("Error reading created_at: " + e.getMessage());
            }
            
            try {
                category.setUpdatedAt(rs.getTimestamp("updated_at"));
            } catch (SQLException e) {
                System.err.println("Error reading updated_at: " + e.getMessage());
            }
            
            return category;
        } catch (SQLException e) {
            System.err.println("Error mapping ResultSet to Category: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Returns all main categories (not subcategories)
     */
    public List<Category> findMainCategories() {
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_MAIN_CATEGORIES);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                try {
                    categories.add(mapResultSetToCategory(rs));
                } catch (SQLException e) {
                    System.err.println("Error mapping main category in findMainCategories: " + e.getMessage());
                    // Continue with next category
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in findMainCategories: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            // Return partially loaded list instead of throwing exception
        }
        return categories;
    }
} 