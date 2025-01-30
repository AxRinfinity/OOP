package com.library.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnectionTest {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTest.class);

    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        System.out.println("URL: " + DatabaseConnection.URL);
        System.out.println("User: " + DatabaseConnection.USER);
        
        try {
            // Test JDBC driver loading
            System.out.println("\nStep 1: Loading JDBC driver...");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("✓ JDBC driver loaded successfully");
            
            // Test connection
            System.out.println("\nStep 2: Attempting to connect to database...");
            Connection conn = DatabaseConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Connection established successfully!");
                
                // Get database metadata
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("\nDatabase Information:");
                System.out.println("Database Product Name: " + metaData.getDatabaseProductName());
                System.out.println("Database Product Version: " + metaData.getDatabaseProductVersion());
                System.out.println("Driver Name: " + metaData.getDriverName());
                System.out.println("Driver Version: " + metaData.getDriverVersion());
                
                // Test if connection is valid
                System.out.println("\nStep 3: Testing if connection is valid...");
                boolean isValid = conn.isValid(5); // 5 seconds timeout
                System.out.println(isValid ? "✓ Connection is valid" : "✗ Connection is invalid");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("\n✗ Failed to load JDBC driver!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("\n✗ Failed to connect to database!");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            
            // Print all chained errors
            SQLException nextException = e.getNextException();
            while (nextException != null) {
                System.err.println("\nChained error:");
                System.err.println("SQL State: " + nextException.getSQLState());
                System.err.println("Error Code: " + nextException.getErrorCode());
                System.err.println("Message: " + nextException.getMessage());
                nextException = nextException.getNextException();
            }
            
            e.printStackTrace();
        } finally {
            System.out.println("\nStep 4: Closing connection...");
            DatabaseConnection.closeConnection();
        }
    }
} 