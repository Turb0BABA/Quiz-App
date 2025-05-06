package com.quizapp.service;

import com.quizapp.dao.AnswerDao;
import com.quizapp.dao.CategoryDAO;
import com.quizapp.dao.QuestionDAO;
import com.quizapp.dao.UserDAO;
import com.quizapp.model.Answer;
import com.quizapp.model.Category;
import com.quizapp.model.Question;
import com.quizapp.model.User;

import java.util.List;
import java.util.Optional;

public class AdminService {
    private final CategoryDAO categoryDAO;
    private final QuestionDAO questionDAO;
    private final AnswerDao answerDao;
    private final UserDAO userDAO;

    public AdminService() {
        this.categoryDAO = new CategoryDAO();
        this.questionDAO = new QuestionDAO();
        this.answerDao = new AnswerDao();
        this.userDAO = new UserDAO();
    }

    // Category CRUD
    public List<Category> getAllCategories() { return categoryDAO.findAll(); }
    public List<Category> getCategoriesWithQuestions() { return categoryDAO.findCategoriesWithQuestions(); }
    public List<Category> getAllCategoriesWithSubcategories() { return categoryDAO.findAllWithSubcategories(); }
    public List<Category> getSubcategories(int parentId) { return categoryDAO.findSubcategories(parentId); }
    public Optional<Category> getCategoryById(int id) { return categoryDAO.findById(id); }
    public Category createCategory(Category c) { return categoryDAO.create(c); }
    public Category createSubcategory(Category c, int parentId) { 
        c.setParentId(parentId);
        c.setSubcategory(true);
        return categoryDAO.create(c); 
    }
    public void updateCategory(Category c) { categoryDAO.update(c); }
    public void deleteCategory(int id) { categoryDAO.delete(id); }
    public int cleanupDuplicateCategories() { return categoryDAO.cleanupDuplicateCategories(); }

    // Question CRUD
    public List<Question> getQuestionsByCategory(int categoryId) { return questionDAO.findByCategoryId(categoryId); }
    public Optional<Question> getQuestionById(int id) { return questionDAO.findById(id); }
    public Question createQuestion(Question q) { return questionDAO.create(q); }
    public void updateQuestion(Question q) { questionDAO.update(q); }
    public void deleteQuestion(int id) { questionDAO.delete(id); }
    public int cleanupDuplicateQuestions() { return questionDAO.cleanupDuplicateQuestions(); }

    // Answers for a question
    public List<Answer> getAnswersForQuestion(int questionId) { return answerDao.findByQuestionId(questionId); }
    
    // User management
    public List<User> getAllUsers() { return userDAO.findAllUsers(); }
    public Optional<User> getUserById(int userId) { return userDAO.findById(userId); }
    public void deleteUser(int userId) { userDAO.deleteUser(userId); }
    public void updateUserStatus(int userId, boolean isActive) { userDAO.updateUserStatus(userId, isActive); }
} 