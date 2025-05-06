-- Updates for analytics and admin features

-- 1. User Management enhancements
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_date TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(100) NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_expiry TIMESTAMP NULL;

-- 2. Analytics tracking
CREATE TABLE IF NOT EXISTS quiz_attempts (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    quiz_id INT NOT NULL,
    category_id INT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    max_score DECIMAL(5,2) NOT NULL, 
    completion_time INT,
    attempt_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE IF NOT EXISTS question_responses (
    response_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_answer_id INT,
    is_correct BOOLEAN NOT NULL,
    time_taken INT,
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(attempt_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
);

-- 3. Content Moderation
CREATE TABLE IF NOT EXISTS pending_questions (
    pending_id INT PRIMARY KEY AUTO_INCREMENT,
    submitter_id INT NOT NULL,
    category_id INT NOT NULL,
    question_text TEXT NOT NULL,
    difficulty INT NOT NULL DEFAULT 1,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    reviewer_id INT,
    review_date TIMESTAMP NULL,
    rejection_reason TEXT,
    FOREIGN KEY (submitter_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (reviewer_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS pending_answers (
    pending_answer_id INT PRIMARY KEY AUTO_INCREMENT,
    pending_question_id INT NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (pending_question_id) REFERENCES pending_questions(pending_id) ON DELETE CASCADE
); 