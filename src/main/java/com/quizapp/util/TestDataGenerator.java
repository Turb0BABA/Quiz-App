package com.quizapp.util;

import com.quizapp.dao.CategoryDAO;
import com.quizapp.dao.QuestionDAO;
import com.quizapp.dao.UserDAO;
import com.quizapp.dao.AnswerDao;
import com.quizapp.model.Category;
import com.quizapp.model.Question;
import com.quizapp.model.User;
import com.quizapp.model.Answer;
import com.quizapp.admin.model.QuizAttempt;
import com.quizapp.admin.dao.QuizAttemptDAO;
import com.quizapp.admin.dao.impl.QuizAttemptDAOImpl;
import com.quizapp.admin.model.PendingQuestion;
import com.quizapp.admin.model.PendingAnswer;
import com.quizapp.admin.dao.PendingQuestionDAO;
import com.quizapp.admin.dao.impl.PendingQuestionDAOImpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to generate test data for the application.
 * This is useful for demonstration, testing, and development.
 */
public class TestDataGenerator {
    private static final Logger LOGGER = Logger.getLogger(TestDataGenerator.class.getName());
    private static final Random RANDOM = new Random();
    
    private final CategoryDAO categoryDAO;
    private final QuestionDAO questionDAO;
    private final AnswerDao answerDao;
    private final UserDAO userDAO;
    private final QuizAttemptDAO quizAttemptDAO;
    private final PendingQuestionDAO pendingQuestionDAO;
    
    public TestDataGenerator() {
        this.categoryDAO = new CategoryDAO();
        this.questionDAO = new QuestionDAO();
        this.answerDao = new AnswerDao();
        this.userDAO = new UserDAO();
        this.quizAttemptDAO = new QuizAttemptDAOImpl();
        this.pendingQuestionDAO = new PendingQuestionDAOImpl();
    }
    
    /**
     * Generate all test data at once
     */
    public void generateAllTestData() {
        LOGGER.info("Generating test data...");
        
        // Generate users
        generateTestUsers(5);
        
        // Generate categories
        List<Category> categories = generateTestCategories();
        
        // Generate questions for each category
        for (Category category : categories) {
            generateTestQuestions(category.getCategoryId(), 15);
        }
        
        // Generate quiz attempts
        generateQuizAttempts(50);
        
        // Generate pending content for moderation
        generatePendingContent(10);
        
        LOGGER.info("Test data generation complete.");
    }
    
    /**
     * Generate test users
     * @param count number of users to generate
     */
    public void generateTestUsers(int count) {
        LOGGER.info("Generating " + count + " test users...");
        
        for (int i = 1; i <= count; i++) {
            User user = new User();
            user.setUsername("testuser" + i);
            user.setPassword("password");
            user.setEmail("testuser" + i + "@example.com");
            user.setFullName("Test User " + i);
            user.setAdmin(i == 1); // Make the first test user an admin
            
            try {
                userDAO.create(user);
                LOGGER.info("Created test user: " + user.getUsername());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Couldn't create test user: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Generate test categories
     * @return list of created categories
     */
    public List<Category> generateTestCategories() {
        LOGGER.info("Generating test categories...");
        List<Category> categories = new ArrayList<>();
        
        // Main categories
        String[] mainCategoryNames = {
            "Programming", "Mathematics", "Science", "History", "Literature"
        };
        
        for (int i = 0; i < mainCategoryNames.length; i++) {
            Category category = new Category();
            category.setName(mainCategoryNames[i]);
            category.setDescription("Questions about " + mainCategoryNames[i]);
            category.setTimePerQuestion(30);
            category.setTotalTime(600);
            category.setDisplayOrder(i + 1);
            
            try {
                category = categoryDAO.create(category);
                categories.add(category);
                LOGGER.info("Created main category: " + category.getName());
                
                // Add subcategories for each main category
                generateSubcategories(category);
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Couldn't create category: " + e.getMessage(), e);
            }
        }
        
        return categories;
    }
    
    /**
     * Generate subcategories for a main category
     * @param mainCategory the parent category
     */
    private void generateSubcategories(Category mainCategory) {
        String[] subcategoryNames;
        
        switch (mainCategory.getName()) {
            case "Programming":
                subcategoryNames = new String[]{"Java", "Python", "JavaScript", "Database"};
                break;
            case "Mathematics":
                subcategoryNames = new String[]{"Algebra", "Geometry", "Calculus", "Statistics"};
                break;
            case "Science":
                subcategoryNames = new String[]{"Physics", "Chemistry", "Biology", "Astronomy"};
                break;
            case "History":
                subcategoryNames = new String[]{"Ancient", "Medieval", "Modern", "World Wars"};
                break;
            case "Literature":
                subcategoryNames = new String[]{"Fiction", "Poetry", "Drama", "Essays"};
                break;
            default:
                subcategoryNames = new String[]{"Subcategory 1", "Subcategory 2"};
        }
        
        for (int i = 0; i < subcategoryNames.length; i++) {
            Category subcategory = new Category();
            subcategory.setName(subcategoryNames[i]);
            subcategory.setDescription(subcategoryNames[i] + " questions in " + mainCategory.getName());
            subcategory.setSubcategory(true);
            subcategory.setParentId(mainCategory.getCategoryId());
            subcategory.setTimePerQuestion(30);
            subcategory.setTotalTime(600);
            subcategory.setDisplayOrder(i + 1);
            
            try {
                subcategory = categoryDAO.create(subcategory);
                LOGGER.info("Created subcategory: " + subcategory.getName() + " under " + mainCategory.getName());
                
                // Add 5 questions to each subcategory
                generateTestQuestions(subcategory.getCategoryId(), 5);
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Couldn't create subcategory: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Generate test questions for a category
     * @param categoryId the category ID
     * @param count number of questions to generate
     */
    public void generateTestQuestions(int categoryId, int count) {
        LOGGER.info("Generating " + count + " test questions for category ID " + categoryId);
        
        Category category = categoryDAO.findById(categoryId).orElse(null);
        if (category == null) {
            LOGGER.warning("Category not found with ID: " + categoryId);
            return;
        }
        
        for (int i = 1; i <= count; i++) {
            Question question = new Question();
            question.setCategoryId(categoryId);
            
            // Generate question text based on category
            String questionText = generateQuestionText(category, i);
            question.setQuestionText(questionText);
            
            // Set difficulty (1-5)
            question.setDifficulty(RANDOM.nextInt(5) + 1);
            
            try {
                // Create the question
                Question createdQuestion = questionDAO.create(question);
                LOGGER.info("Created test question ID: " + createdQuestion.getQuestionId());
                
                // Add answers (1 correct, 3 incorrect)
                createAnswers(createdQuestion.getQuestionId(), category);
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Couldn't create question: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Generate appropriate question text based on category
     */
    private String generateQuestionText(Category category, int number) {
        if (category.isSubcategory() && category.getParentId() != null) {
            Category parent = categoryDAO.findById(category.getParentId()).orElse(null);
            if (parent != null) {
                switch (parent.getName()) {
                    case "Programming":
                        return generateProgrammingQuestion(category.getName(), number);
                    case "Mathematics":
                        return generateMathQuestion(category.getName(), number);
                    case "Science":
                        return generateScienceQuestion(category.getName(), number);
                    case "History":
                        return generateHistoryQuestion(category.getName(), number);
                    case "Literature":
                        return generateLiteratureQuestion(category.getName(), number);
                }
            }
        }
        
        // Default question if no specific generator applies
        return "Question " + number + " about " + category.getName() + "?";
    }
    
    private String generateProgrammingQuestion(String subcategory, int number) {
        switch (subcategory) {
            case "Java":
                String[] javaQuestions = {
                    "What is the main method signature in Java?",
                    "Which keyword is used for inheritance in Java?",
                    "What is an interface in Java?",
                    "What is the difference between == and .equals() in Java?",
                    "What is a Java package?"
                };
                return number <= javaQuestions.length ? javaQuestions[number - 1] : "Java question #" + number;
            case "Python":
                return "What is a Python " + (number % 5 == 0 ? "decorator" : number % 4 == 0 ? "list comprehension" : number % 3 == 0 ? "dictionary" : number % 2 == 0 ? "tuple" : "function") + "?";
            case "JavaScript":
                return "How does JavaScript handle " + (number % 5 == 0 ? "closures" : number % 4 == 0 ? "promises" : number % 3 == 0 ? "async/await" : number % 2 == 0 ? "DOM manipulation" : "prototypal inheritance") + "?";
            case "Database":
                return "What is the purpose of " + (number % 5 == 0 ? "normalization" : number % 4 == 0 ? "indexes" : number % 3 == 0 ? "transactions" : number % 2 == 0 ? "stored procedures" : "primary keys") + " in databases?";
            default:
                return "Programming question about " + subcategory + " #" + number;
        }
    }
    
    private String generateMathQuestion(String subcategory, int number) {
        switch (subcategory) {
            case "Algebra":
                return "Solve for x: " + number + "x + " + (number * 2) + " = " + (number * 5);
            case "Geometry":
                return "What is the formula for the " + (number % 3 == 0 ? "volume" : number % 2 == 0 ? "surface area" : "perimeter") + " of a " + (number % 5 == 0 ? "sphere" : number % 4 == 0 ? "cube" : number % 3 == 0 ? "cylinder" : number % 2 == 0 ? "cone" : "rectangle") + "?";
            case "Calculus":
                return "Find the " + (number % 2 == 0 ? "derivative" : "integral") + " of f(x) = " + number + "x^2 + " + (number + 1) + "x + " + (number * 2);
            case "Statistics":
                return "What is the " + (number % 5 == 0 ? "standard deviation" : number % 4 == 0 ? "mean" : number % 3 == 0 ? "median" : number % 2 == 0 ? "mode" : "variance") + " of a data set?";
            default:
                return "Math question about " + subcategory + " #" + number;
        }
    }
    
    private String generateScienceQuestion(String subcategory, int number) {
        switch (subcategory) {
            case "Physics":
                return "What is " + (number % 5 == 0 ? "Newton's third law" : number % 4 == 0 ? "the theory of relativity" : number % 3 == 0 ? "Ohm's law" : number % 2 == 0 ? "quantum mechanics" : "gravity") + "?";
            case "Chemistry":
                return "Explain " + (number % 5 == 0 ? "ionic bonding" : number % 4 == 0 ? "covalent bonding" : number % 3 == 0 ? "the periodic table" : number % 2 == 0 ? "acids and bases" : "atomic structure") + ".";
            case "Biology":
                return "Describe " + (number % 5 == 0 ? "photosynthesis" : number % 4 == 0 ? "cellular respiration" : number % 3 == 0 ? "DNA replication" : number % 2 == 0 ? "natural selection" : "the human nervous system") + ".";
            case "Astronomy":
                return "What is " + (number % 5 == 0 ? "a black hole" : number % 4 == 0 ? "a neutron star" : number % 3 == 0 ? "the Big Bang theory" : number % 2 == 0 ? "a galaxy" : "a planet") + "?";
            default:
                return "Science question about " + subcategory + " #" + number;
        }
    }
    
    private String generateHistoryQuestion(String subcategory, int number) {
        switch (subcategory) {
            case "Ancient":
                return "Who was " + (number % 5 == 0 ? "Julius Caesar" : number % 4 == 0 ? "Alexander the Great" : number % 3 == 0 ? "Cleopatra" : number % 2 == 0 ? "Socrates" : "Confucius") + "?";
            case "Medieval":
                return "What was " + (number % 5 == 0 ? "the Crusades" : number % 4 == 0 ? "the Black Death" : number % 3 == 0 ? "feudalism" : number % 2 == 0 ? "the Magna Carta" : "the Holy Roman Empire") + "?";
            case "Modern":
                return "Explain the significance of " + (number % 5 == 0 ? "the Industrial Revolution" : number % 4 == 0 ? "the French Revolution" : number % 3 == 0 ? "the American Civil War" : number % 2 == 0 ? "the Cold War" : "colonialism") + ".";
            case "World Wars":
                return "What caused " + (number % 2 == 0 ? "World War I" : "World War II") + "?";
            default:
                return "History question about " + subcategory + " #" + number;
        }
    }
    
    private String generateLiteratureQuestion(String subcategory, int number) {
        switch (subcategory) {
            case "Fiction":
                return "Who wrote '" + (number % 5 == 0 ? "Pride and Prejudice" : number % 4 == 0 ? "The Great Gatsby" : number % 3 == 0 ? "To Kill a Mockingbird" : number % 2 == 0 ? "1984" : "War and Peace") + "'?";
            case "Poetry":
                return "What is " + (number % 5 == 0 ? "a sonnet" : number % 4 == 0 ? "blank verse" : number % 3 == 0 ? "free verse" : number % 2 == 0 ? "a haiku" : "an epic poem") + "?";
            case "Drama":
                return "Who wrote the play '" + (number % 5 == 0 ? "Hamlet" : number % 4 == 0 ? "Death of a Salesman" : number % 3 == 0 ? "A Streetcar Named Desire" : number % 2 == 0 ? "Waiting for Godot" : "Oedipus Rex") + "'?";
            case "Essays":
                return "What characterizes " + (number % 3 == 0 ? "a persuasive essay" : number % 2 == 0 ? "a narrative essay" : "an expository essay") + "?";
            default:
                return "Literature question about " + subcategory + " #" + number;
        }
    }
    
    /**
     * Create answers for a question
     * @param questionId the question ID
     */
    private void createAnswers(int questionId, Category category) {
        String[] answers;
        boolean[] isCorrect;
        
        // Generate appropriate answers based on question
        Question question = questionDAO.findById(questionId).orElse(null);
        if (question == null) {
            return;
        }
        
        answers = generateAnswers(question, category);
        isCorrect = new boolean[]{true, false, false, false};
        
        for (int i = 0; i < answers.length; i++) {
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answers[i]);
            answer.setCorrect(isCorrect[i]);
            
            try {
                answerDao.create(answer);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Couldn't create answer: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Generate appropriate answers based on the question
     */
    private String[] generateAnswers(Question question, Category category) {
        String questionText = question.getQuestionText().toLowerCase();
        
        // Basic placeholder answers
        String[] defaultAnswers = {
            "Answer A (Correct)",
            "Answer B",
            "Answer C",
            "Answer D"
        };
        
        // Try to generate specific answers for some common questions
        if (questionText.contains("main method signature in java")) {
            return new String[]{
                "public static void main(String[] args)",
                "public void main(String[] args)",
                "public static int main(String[] args)",
                "void main(String[] args)"
            };
        } else if (questionText.contains("which keyword is used for inheritance in java")) {
            return new String[]{
                "extends",
                "implements",
                "inherits",
                "using"
            };
        } else if (questionText.contains("what is an interface in java")) {
            return new String[]{
                "A contract that specifies methods a class must implement",
                "A class that cannot be instantiated",
                "A collection of static methods",
                "A type of Java package"
            };
        }
        
        return defaultAnswers;
    }
    
    /**
     * Generate quiz attempts for testing analytics
     * @param count number of attempts to generate
     */
    public void generateQuizAttempts(int count) {
        LOGGER.info("Generating " + count + " test quiz attempts...");
        
        // Get a list of users and categories
        List<User> users = userDAO.findAllUsers();
        List<Category> categories = categoryDAO.findAll();
        
        if (users.isEmpty() || categories.isEmpty()) {
            LOGGER.warning("No users or categories found. Cannot generate quiz attempts.");
            return;
        }
        
        // Generate random quiz attempts
        for (int i = 1; i <= count; i++) {
            User user = users.get(RANDOM.nextInt(users.size()));
            Category category = categories.get(RANDOM.nextInt(categories.size()));
            
            QuizAttempt attempt = new QuizAttempt();
            attempt.setUserId(user.getUserId());
            attempt.setQuizId(i); // Use the attempt number as a placeholder quiz ID
            attempt.setCategoryId(category.getCategoryId());
            
            // Random score between 0 and 100, with 100 max score
            double score = RANDOM.nextInt(101);
            attempt.setScore(score);
            attempt.setMaxScore(100.0);
            
            // Random completion time between 1 and 15 minutes (in seconds)
            int completionTime = RANDOM.nextInt(14 * 60) + 60;
            attempt.setCompletionTime(completionTime);
            
            // Random date in the past 30 days
            int daysAgo = RANDOM.nextInt(30);
            attempt.setAttemptDate(Timestamp.valueOf(LocalDateTime.now().minusDays(daysAgo)));
            
            try {
                quizAttemptDAO.create(attempt);
                LOGGER.info("Created test quiz attempt for user " + user.getUsername() + " in category " + category.getName());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Couldn't create quiz attempt: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Generate pending content for moderation
     * @param count number of pending questions to generate
     */
    public void generatePendingContent(int count) {
        LOGGER.info("Generating " + count + " pending questions for moderation...");
        
        // Get a list of users and categories
        List<User> users = userDAO.findAllUsers();
        List<Category> categories = categoryDAO.findAll();
        
        if (users.isEmpty() || categories.isEmpty()) {
            LOGGER.warning("No users or categories found. Cannot generate pending content.");
            return;
        }
        
        // Generate random pending questions with answers
        for (int i = 1; i <= count; i++) {
            User submitter = users.get(RANDOM.nextInt(users.size()));
            Category category = categories.get(RANDOM.nextInt(categories.size()));
            
            PendingQuestion pendingQuestion = new PendingQuestion();
            pendingQuestion.setSubmitterId(submitter.getUserId());
            pendingQuestion.setCategoryId(category.getCategoryId());
            pendingQuestion.setQuestionText("Pending question #" + i + " about " + category.getName());
            pendingQuestion.setDifficulty(RANDOM.nextInt(5) + 1);
            
            // Add status - make some approved, some rejected, some pending
            int statusRandom = RANDOM.nextInt(3);
            String status = statusRandom == 0 ? "approved" : (statusRandom == 1 ? "rejected" : "pending");
            pendingQuestion.setStatus(status);
            
            if (!status.equals("pending")) {
                // If approved or rejected, set reviewer
                User reviewer = users.get(RANDOM.nextInt(users.size()));
                pendingQuestion.setReviewerId(reviewer.getUserId());
                pendingQuestion.setReviewDate(new Timestamp(System.currentTimeMillis()));
                
                if (status.equals("rejected")) {
                    pendingQuestion.setRejectionReason("This question is too " + 
                        (RANDOM.nextBoolean() ? "simple" : "complex") + " for our quiz.");
                }
            }
            
            try {
                pendingQuestion = pendingQuestionDAO.create(pendingQuestion);
                LOGGER.info("Created test pending question ID: " + pendingQuestion.getPendingId());
                
                // Add answers (1 correct, 3 incorrect)
                for (int j = 0; j < 4; j++) {
                    PendingAnswer answer = new PendingAnswer();
                    answer.setPendingQuestionId(pendingQuestion.getPendingId());
                    answer.setAnswerText("Answer " + (char)('A' + j) + " for pending question #" + i);
                    answer.setCorrect(j == 0); // First answer is correct
                    
                    pendingQuestion.addAnswer(answer);
                }
                
                LOGGER.info("Added answers to pending question ID: " + pendingQuestion.getPendingId());
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Couldn't create pending question: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Main method to run the data generator
     */
    public static void main(String[] args) {
        TestDataGenerator generator = new TestDataGenerator();
        generator.generateAllTestData();
    }
} 