package com.quizapp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Initialize main schema
            String schema = loadSchemaFile("quiz_db.sql");
            executeSchema(conn, schema);
            System.out.println("Database schema initialized successfully");
            
            // Check if updates file exists first
            InputStream updateStream = DatabaseInitializer.class.getClassLoader().getResourceAsStream("db_update.sql");
            if (updateStream != null) {
                try {
                    updateStream.close();
                    String updateSchema = loadSchemaFile("db_update.sql");
                    executeSchema(conn, updateSchema);
                    System.out.println("Database schema updates applied successfully");
                } catch (IOException e) {
                    System.err.println("Error reading db_update.sql: " + e.getMessage());
                }
            } else {
                // File doesn't exist, but this is normal for initial setup
                System.out.println("No schema updates file found (this is normal for initial setup)");
            }
        } catch (SQLException | IOException e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static String loadSchemaFile(String filename) throws IOException {
        try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                throw new IOException("Could not find " + filename);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    private static void executeSchema(Connection conn, String schema) throws SQLException {
        // Split the schema into individual statements using a more reliable delimiter
        String[] statements = schema.split(";");
        
        try (Statement stmt = conn.createStatement()) {
            for (String statement : statements) {
                String trimmedStmt = statement.trim();
                if (!trimmedStmt.isEmpty()) {
                    try {
                        stmt.execute(trimmedStmt);
                    } catch (SQLException e) {
                        // Print the statement that caused the error
                        System.err.println("Error executing SQL statement: " + trimmedStmt);
                        System.err.println("SQL Error: " + e.getMessage());
                        // Only throw the exception for critical errors
                        if (e.getMessage().contains("Access denied") || 
                            e.getMessage().contains("Unknown database")) {
                        throw e;
                        }
                        // For other errors, log and continue
                        System.out.println("Continuing despite SQL error...");
                    }
                }
            }
        }
    }
} 