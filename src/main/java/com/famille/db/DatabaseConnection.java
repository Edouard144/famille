package com.famille.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Your Neon DB connection URL — get this from neon.tech dashboard
    // Format: jdbc:postgresql://<host>/<dbname>?sslmode=require
    private static final String URL = "postgresql://neondb_owner:npg_eJpT3g5bqNxu@ep-young-field-ad02i6kh-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require";
    private static final String USER = "Silent";
    private static final String PASSWORD = "yuSui8QDQrt.E-V";

    // This method gives any class a fresh database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}