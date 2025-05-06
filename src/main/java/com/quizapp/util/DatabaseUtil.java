package com.quizapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseUtil {
    private static final Properties properties = new Properties();
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    static {
        try {
            // Load the properties file
            try (InputStream input = DatabaseUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    System.err.println("config.properties file not found in the classpath");
                    throw new RuntimeException("Unable to find config.properties");
                }
                properties.load(input);
                System.out.println("Successfully loaded config.properties");
            }

            // Load the JDBC driver
            Class.forName(properties.getProperty("db.driver"));
            System.out.println("Successfully loaded JDBC driver: " + properties.getProperty("db.driver"));

            // Set connection properties
            URL = properties.getProperty("db.url");
            USERNAME = properties.getProperty("db.username");
            PASSWORD = properties.getProperty("db.password");

            // Display connection info for troubleshooting (remove in production)
            System.out.println("Database URL: " + URL);
            System.out.println("Database User: " + USERNAME);

            // Test connection
            testConnection();

        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load database driver: " + e.getMessage());
            throw new RuntimeException("Failed to load database driver. Make sure MySQL is installed and the driver is in your classpath.", e);
        } catch (SQLException e) {
            System.err.println("SQL connection error: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            throw new RuntimeException("Failed to connect to database. Message: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Initialization error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to initialize database configuration: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            throw e;
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            if (!conn.isValid(5)) { // Test if connection is valid with 5 second timeout
                throw new SQLException("Database connection test failed");
            }
            System.out.println("Database connection test successful");
        }
    }
} 