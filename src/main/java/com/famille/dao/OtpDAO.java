package com.famille.dao;

import com.famille.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class OtpDAO {

    /**
     * Save a new OTP code to the database.
     * Called right after we generate the code and before we email it.
     *
     * @param userId  the user this code belongs to
     * @param code    the 6-digit code
     */
    public void saveOtp(int userId, String code) throws SQLException {

        String sql = "INSERT INTO otp_codes (user_id, code, expires_at, used) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, code);

            // Expires exactly 10 minutes from now
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));

            stmt.setBoolean(4, false); // not used yet

            stmt.executeUpdate();
        }
    }

    /**
     * Check if an OTP code is valid for this user.
     * Valid means: correct code + not expired + not already used.
     *
     * @param userId  the user trying to verify
     * @param code    the code they typed in the app
     * @return        the OTP row ID if valid, -1 if invalid
     */
    public int findValidOtp(int userId, String code) throws SQLException {

        String sql = """
                SELECT id FROM otp_codes
                WHERE user_id = ?
                  AND code = ?
                  AND used = FALSE
                  AND expires_at > NOW()
                ORDER BY id DESC
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, code);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id"); // return the row ID so we can mark it used
            }
        }
        return -1; // nothing found — invalid or expired
    }

    /**
     * Mark an OTP as used so it cannot be reused.
     * Called immediately after a successful verification.
     *
     * @param otpId  the row ID from findValidOtp()
     */
    public void markUsed(int otpId) throws SQLException {

        String sql = "UPDATE otp_codes SET used = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, otpId);
            stmt.executeUpdate();
        }
    }
}