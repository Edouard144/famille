package com.famille.dao;

import com.famille.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class PushLogDAO {

    /**
     * Log every push notification attempt to the database.
     * The admin dashboard uses this to show what was sent and when.
     */
    public void logPush(int familyId, String title, String body, String status) {

        String sql = """
                INSERT INTO push_logs (family_id, title, body, sent_at, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, familyId);
            stmt.setString(2, title);
            stmt.setString(3, body);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(5, status);

            stmt.executeUpdate();

        } catch (SQLException e) {
            // Don't crash the app if logging fails — just print the error
            System.err.println("❌ Push log failed: " + e.getMessage());
        }
    }
}