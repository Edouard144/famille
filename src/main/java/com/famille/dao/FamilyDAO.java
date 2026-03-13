package com.famille.dao;

import com.famille.db.DatabaseConnection;

import java.sql.*;

public class FamilyDAO {

    /**
     * Get a family's ID from the user's ID.
     * Every parent has one family — this links them.
     * If no family exists yet, create one automatically.
     */
    public int getFamilyIdByUserId(int userId) throws SQLException {

        // First try to find existing family
        String selectSql = "SELECT id FROM families WHERE user_id = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id"); // found — return it
            }
        }

        // No family yet — create one automatically on first use
        String insertSql = "INSERT INTO families (user_id, family_name) VALUES (?, 'Umuryango') RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return rs.getInt("id");
        }

        throw new SQLException("Umuryango ntiwashyizweho. (Family creation failed)");
    }
}