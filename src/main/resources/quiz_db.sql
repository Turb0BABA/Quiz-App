-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS quiz_db;

-- Create the user if it doesn't exist
-- CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED BY 'ajar10gupta';

-- Grant privileges to the user
-- GRANT ALL PRIVILEGES ON quiz_db.* TO 'root'@'localhost';

-- Make sure the privileges are applied
-- FLUSH PRIVILEGES; 

USE quiz_db;

-- Drop tables if they exist to avoid constraint issues
DROP TABLE IF EXISTS quiz_results;
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    is_admin BOOLEAN DEFAULT 0,
    full_name VARCHAR(100),
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Create categories table without foreign key constraint initially
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id INT DEFAULT NULL,
    is_subcategory BOOLEAN DEFAULT FALSE,
    time_per_question INT DEFAULT 30,
    total_time INT DEFAULT 600,
    icon_path VARCHAR(255) DEFAULT NULL,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_is_subcategory (is_subcategory),
    INDEX idx_name (name)
);

-- Create questions table
CREATE TABLE IF NOT EXISTS questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    question_text TEXT NOT NULL,
    options TEXT NOT NULL,
    correct_option_index INT NOT NULL,
    difficulty_level VARCHAR(20),
    points INT DEFAULT 1,
    is_flagged BOOLEAN DEFAULT FALSE,
    flag_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- Create answers table
CREATE TABLE IF NOT EXISTS answers (
    answer_id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT 0,
    INDEX idx_question_id (question_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- Create quiz_results table
CREATE TABLE IF NOT EXISTS quiz_results (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    time_taken INT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

-- Add the self-referencing foreign key constraint to categories table after all tables are created
ALTER TABLE categories 
ADD CONSTRAINT fk_category_parent 
FOREIGN KEY (parent_id) REFERENCES categories(category_id) 
ON DELETE SET NULL;

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, email, is_admin, is_active)
VALUES ('admin', 'admin123', 'admin@quizapp.com', TRUE, TRUE)
ON DUPLICATE KEY UPDATE password = 'admin123', is_active = TRUE;

-- Insert main category
INSERT INTO categories (name, description, is_subcategory, display_order)
VALUES ('Java Quiz', 'Comprehensive Java programming quizzes', FALSE, 1)
ON DUPLICATE KEY UPDATE category_id = category_id;

-- Get the ID of the main category
SET @java_category_id = LAST_INSERT_ID();

-- Insert subcategories for Java Quiz
INSERT INTO categories (name, description, parent_id, is_subcategory, display_order)
VALUES 
    ('Basic', 'Java fundamentals and basic concepts', @java_category_id, TRUE, 1),
    ('Operator/Condition', 'Java operators and conditional statements', @java_category_id, TRUE, 2),
    ('Tables/Loops', 'Arrays, collections and loops in Java', @java_category_id, TRUE, 3),
    ('OOP (Object Oriented Programming)', 'Object-oriented programming concepts in Java', @java_category_id, TRUE, 4),
    ('Polymorphism/Enum', 'Polymorphism and enumeration in Java', @java_category_id, TRUE, 5),
    ('Exceptions', 'Exception handling in Java', @java_category_id, TRUE, 6),
    ('Advanced', 'Advanced Java programming concepts', @java_category_id, TRUE, 7)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- Insert sample questions for Basic subcategory
INSERT INTO questions (category_id, question_text, options, correct_option_index)
SELECT c.category_id, 'What is Java?', 'A programming language|A coffee brand|A type of computer|A game', 0
FROM categories c WHERE c.name = 'Basic' AND c.is_subcategory = TRUE
ON DUPLICATE KEY UPDATE question_id = question_id;

INSERT INTO questions (category_id, question_text, options, correct_option_index)
SELECT c.category_id, 'What is JVM?', 'Java Virtual Machine|Java Version Manager|Java Visual Machine|Java Virtual Memory', 0
FROM categories c WHERE c.name = 'Basic' AND c.is_subcategory = TRUE
ON DUPLICATE KEY UPDATE question_id = question_id;

INSERT INTO questions (category_id, question_text, options, correct_option_index)
SELECT c.category_id, 'What is JDK?', 'Java Development Kit|Java Development Knowledge|Java Development Keyboard|Java Development Kernel', 0
FROM categories c WHERE c.name = 'Basic' AND c.is_subcategory = TRUE
ON DUPLICATE KEY UPDATE question_id = question_id; 

