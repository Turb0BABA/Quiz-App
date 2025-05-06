package com.quizapp.config;

import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find config.properties");
                throw new RuntimeException("Database configuration file not found");
            }
            properties.load(input);
        } catch (Exception ex) {
            System.err.println("Error loading database configuration: " + ex.getMessage());
            throw new RuntimeException("Failed to load database configuration", ex);
        }
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public static String getDbUsername() {
        return properties.getProperty("db.username");
    }

    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }

    public static String getDbDriver() {
        return properties.getProperty("db.driver");
    }
} 