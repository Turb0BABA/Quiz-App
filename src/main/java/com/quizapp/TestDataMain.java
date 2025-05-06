package com.quizapp;

import com.quizapp.util.TestDataGenerator;

/**
 * Main class for generating test data.
 * This can be run separately to populate the database with sample data.
 */
public class TestDataMain {
    
    public static void main(String[] args) {
        System.out.println("Starting test data generation...");
        
        TestDataGenerator generator = new TestDataGenerator();
        generator.generateAllTestData();
        
        System.out.println("Test data generation complete!");
    }
} 