package com.famille.dao;

import com.famille.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class SMSLogDAO {

    /**
     * Log every SMS attempt to the database.
     * The admin dashboard reads this to show SMS history.
     */
    public void logSMS(int familyId, String message, String phone, String status) {

        String sql = """
                INSERT INTO sms_logs (family_id, message, phone, sent_at, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, familyId);
            stmt.setString(2, message);
            stmt.setString(3, phone);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(5, status);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ SMS log failed: " + e.getMessage());
        }
    }
}