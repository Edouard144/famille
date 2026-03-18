package com.famille.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Driver;
import java.util.Enumeration;

public class DatabaseConnection {

    // Your Neon DB connection URL
    private static final String URL = "jdbc:postgresql://ep-young-field-ad02i6kh-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require";
    private static final String USER = "Silent";
    private static final String PASSWORD = "yuSui8QDQrt.E-V";

    // Static block runs when class is first loaded
    static {
        try {
            // Explicitly load the PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL driver successfully registered!");

            // Debug: List all available drivers
            System.out.println("📋 Available JDBC Drivers:");
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver d = drivers.nextElement();
                System.out.println("   - " + d.getClass().getName());
            }

        } catch (ClassNotFoundException e) {
            System.err.println("❌ CRITICAL: PostgreSQL Driver not found in classpath!");
            System.err.println("   Make sure postgresql-*.jar is in Tomcat/lib or WEB-INF/lib");
            e.printStackTrace();
        }
    }

    // This method gives any class a fresh database connection
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connection successful!");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("   URL: " + URL);
            System.err.println("   User: " + USER);
            System.err.println("   Error: " + e.getMessage());
            throw e; // Re-throw the exception
        }
    }

    // Optional: Test method to verify connection
    public static void main(String[] args) {
        try {
            Connection conn = getConnection();
            System.out.println("Connection test passed!");
            conn.close();
        } catch (SQLException e) {
            System.err.println("Connection test failed!");
            e.printStackTrace();
        }
    }
}