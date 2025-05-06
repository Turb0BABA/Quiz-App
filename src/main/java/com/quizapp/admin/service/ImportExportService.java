package com.quizapp.admin.service;

import com.quizapp.model.Question;
import com.quizapp.model.Answer;
import com.quizapp.model.Category;
import com.quizapp.dao.QuestionDAO;
import com.quizapp.dao.AnswerDao;
import com.quizapp.dao.CategoryDAO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for importing and exporting questions
 */
public class ImportExportService {
    private static final Logger LOGGER = Logger.getLogger(ImportExportService.class.getName());
    
    private final QuestionDAO questionDAO;
    private final AnswerDao answerDao;
    private final CategoryDAO categoryDAO;
    private final ObjectMapper objectMapper;
    
    public ImportExportService() {
        this.questionDAO = new QuestionDAO();
        this.answerDao = new AnswerDao();
        this.categoryDAO = new CategoryDAO();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Export questions to CSV format
     * @param categoryId the category ID to export questions for (or null for all categories)
     * @param outputFile the output file to write to
     * @return number of questions exported
     * @throws IOException if an I/O error occurs
     */
    public int exportQuestionsToCSV(Integer categoryId, File outputFile) throws IOException {
        // CSV headers
        String[] headers = {"question_id", "category_id", "category_name", "question_text", "difficulty", "correct_answer", "incorrect_answer1", "incorrect_answer2", "incorrect_answer3"};
        
        List<Question> questions;
        if (categoryId != null) {
            questions = questionDAO.findByCategoryId(categoryId);
        } else {
            questions = questionDAO.findAll();
        }
        
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputFile), CSVFormat.DEFAULT.withHeader(headers))) {
            for (Question question : questions) {
                List<Answer> answers = answerDao.findByQuestionId(question.getQuestionId());
                Category category = categoryDAO.findById(question.getCategoryId()).orElse(null);
                String categoryName = category != null ? category.getName() : "";
                
                // Find correct and incorrect answers
                Answer correctAnswer = null;
                List<Answer> incorrectAnswers = new ArrayList<>();
                
                for (Answer answer : answers) {
                    if (answer.isCorrect()) {
                        correctAnswer = answer;
                    } else {
                        incorrectAnswers.add(answer);
                    }
                }
                
                // Prepare CSV record
                List<String> record = new ArrayList<>();
                record.add(String.valueOf(question.getQuestionId()));
                record.add(String.valueOf(question.getCategoryId()));
                record.add(categoryName);
                record.add(question.getQuestionText());
                record.add(String.valueOf(question.getDifficulty()));
                record.add(correctAnswer != null ? correctAnswer.getAnswerText() : "");
                
                // Add up to 3 incorrect answers
                for (int i = 0; i < 3; i++) {
                    if (i < incorrectAnswers.size()) {
                        record.add(incorrectAnswers.get(i).getAnswerText());
                    } else {
                        record.add("");
                    }
                }
                
                printer.printRecord(record);
            }
        }
        
        return questions.size();
    }
    
    /**
     * Import questions from CSV format
     * @param inputFile the input file to read from
     * @param categoryId the category ID to import questions into
     * @return number of questions imported
     * @throws IOException if an I/O error occurs
     */
    public int importQuestionsFromCSV(File inputFile, int categoryId) throws IOException {
        int imported = 0;
        
        try (CSVParser parser = CSVParser.parse(inputFile, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                try {
                    Question question = new Question();
                    question.setCategoryId(categoryId);
                    question.setQuestionText(record.get("question_text"));
                    question.setDifficulty(Integer.parseInt(record.get("difficulty")));
                    
                    // Create the question
                    Question createdQuestion = questionDAO.create(question);
                    
                    // Add correct answer
                    String correctAnswerText = record.get("correct_answer");
                    if (!correctAnswerText.isEmpty()) {
                        Answer correctAnswer = new Answer();
                        correctAnswer.setQuestionId(createdQuestion.getQuestionId());
                        correctAnswer.setAnswerText(correctAnswerText);
                        correctAnswer.setCorrect(true);
                        answerDao.create(correctAnswer);
                    }
                    
                    // Add incorrect answers
                    for (int i = 1; i <= 3; i++) {
                        String incorrectAnswerText = record.get("incorrect_answer" + i);
                        if (incorrectAnswerText != null && !incorrectAnswerText.isEmpty()) {
                            Answer incorrectAnswer = new Answer();
                            incorrectAnswer.setQuestionId(createdQuestion.getQuestionId());
                            incorrectAnswer.setAnswerText(incorrectAnswerText);
                            incorrectAnswer.setCorrect(false);
                            answerDao.create(incorrectAnswer);
                        }
                    }
                    
                    imported++;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error importing question from CSV: " + e.getMessage(), e);
                }
            }
        }
        
        return imported;
    }
    
    /**
     * Export questions to JSON format
     * @param categoryId the category ID to export questions for (or null for all categories)
     * @param outputFile the output file to write to
     * @return number of questions exported
     * @throws IOException if an I/O error occurs
     */
    public int exportQuestionsToJSON(Integer categoryId, File outputFile) throws IOException {
        List<Question> questions;
        if (categoryId != null) {
            questions = questionDAO.findByCategoryId(categoryId);
        } else {
            questions = questionDAO.findAll();
        }
        
        ArrayNode questionsArray = objectMapper.createArrayNode();
        
        for (Question question : questions) {
            ObjectNode questionNode = objectMapper.createObjectNode();
            questionNode.put("question_id", question.getQuestionId());
            questionNode.put("category_id", question.getCategoryId());
            
            // Get category name
            Category category = categoryDAO.findById(question.getCategoryId()).orElse(null);
            questionNode.put("category_name", category != null ? category.getName() : "");
            
            questionNode.put("question_text", question.getQuestionText());
            questionNode.put("difficulty", question.getDifficulty());
            
            // Get answers
            List<Answer> answers = answerDao.findByQuestionId(question.getQuestionId());
            ArrayNode answersArray = objectMapper.createArrayNode();
            
            for (Answer answer : answers) {
                ObjectNode answerNode = objectMapper.createObjectNode();
                answerNode.put("answer_text", answer.getAnswerText());
                answerNode.put("is_correct", answer.isCorrect());
                answersArray.add(answerNode);
            }
            
            questionNode.set("answers", answersArray);
            questionsArray.add(questionNode);
        }
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, questionsArray);
        return questions.size();
    }
    
    /**
     * Import questions from JSON format
     * @param inputFile the input file to read from
     * @param categoryId the category ID to import questions into
     * @return number of questions imported
     * @throws IOException if an I/O error occurs
     */
    public int importQuestionsFromJSON(File inputFile, int categoryId) throws IOException {
        int imported = 0;
        
        ArrayNode questionsArray = (ArrayNode) objectMapper.readTree(inputFile);
        
        for (int i = 0; i < questionsArray.size(); i++) {
            try {
                ObjectNode questionNode = (ObjectNode) questionsArray.get(i);
                
                Question question = new Question();
                question.setCategoryId(categoryId);
                question.setQuestionText(questionNode.get("question_text").asText());
                question.setDifficulty(questionNode.get("difficulty").asInt());
                
                // Create the question
                Question createdQuestion = questionDAO.create(question);
                
                // Add answers
                ArrayNode answersArray = (ArrayNode) questionNode.get("answers");
                if (answersArray != null) {
                    for (int j = 0; j < answersArray.size(); j++) {
                        ObjectNode answerNode = (ObjectNode) answersArray.get(j);
                        
                        Answer answer = new Answer();
                        answer.setQuestionId(createdQuestion.getQuestionId());
                        answer.setAnswerText(answerNode.get("answer_text").asText());
                        answer.setCorrect(answerNode.get("is_correct").asBoolean());
                        
                        answerDao.create(answer);
                    }
                }
                
                imported++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error importing question from JSON: " + e.getMessage(), e);
            }
        }
        
        return imported;
    }
    
    /**
     * Check if a file has CSV extension
     * @param file the file to check
     * @return true if the file has CSV extension
     */
    public boolean isCSVFile(File file) {
        return file.getName().toLowerCase().endsWith(".csv");
    }
    
    /**
     * Check if a file has JSON extension
     * @param file the file to check
     * @return true if the file has JSON extension
     */
    public boolean isJSONFile(File file) {
        return file.getName().toLowerCase().endsWith(".json");
    }
} 