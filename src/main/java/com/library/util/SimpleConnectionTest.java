package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=library;encrypt=true;trustServerCertificate=true;integratedSecurity=false;loginTimeout=30";
        String user = "AxR";
        String password = "2025makeris";

        System.out.println("Testing database connection...");
        System.out.println("URL: " + url);
        System.out.println("User: " + user);
        
        try {
            // Load driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("Driver loaded successfully");
            
            // Try to connect
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection successful!");
            
            // Close connection
            if (conn != null) {
                conn.close();
                System.out.println("Connection closed successfully");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading driver: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database error:");
            System.out.println("Error code: " + e.getErrorCode());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 