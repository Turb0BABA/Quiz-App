-- Database update script to add is_active column to users table
USE quiz_db;

-- Check if the is_active column already exists
SET @exists = 0;
SELECT 1 INTO @exists FROM information_schema.columns 
WHERE table_schema = 'quiz_db' AND table_name = 'users' AND column_name = 'is_active';

-- Add the is_active column if it doesn't exist
SET @query = IF(@exists = 0, 
    'ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT 1 AFTER full_name', 
    'SELECT "is_active column already exists" as message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt; 