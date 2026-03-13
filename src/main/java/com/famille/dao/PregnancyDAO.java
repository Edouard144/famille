package com.famille.dao;

import com.famille.db.DatabaseConnection;
import com.famille.models.Pregnancy;

import java.sql.*;
import java.time.LocalDate;

public class PregnancyDAO {

    /** Register a new pregnancy. */
    public int createPregnancy(Pregnancy pregnancy) throws SQLException {

        String sql = "INSERT INTO pregnancies (family_id, due_date, current_week) " +
                "VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pregnancy.getFamilyId());
            stmt.setDate(2, Date.valueOf(pregnancy.getDueDate()));
            stmt.setInt(3, pregnancy.calculateCurrentWeek()); // auto-calculate

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        throw new SQLException("Inda ntiyashyizweho. (Pregnancy creation failed)");
    }

    /** Get the current active pregnancy for a family. */
    public Pregnancy getByFamilyId(int familyId) throws SQLException {

        // Get the most recent pregnancy for this family
        String sql = "SELECT * FROM pregnancies WHERE family_id = ? ORDER BY id DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, familyId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Pregnancy p = new Pregnancy();
                p.setId(rs.getInt("id"));
                p.setFamilyId(rs.getInt("family_id"));
                p.setCurrentWeek(rs.getInt("current_week"));

                Date dueDate = rs.getDate("due_date");
                if (dueDate != null) p.setDueDate(dueDate.toLocalDate());

                return p;
            }
        }
        return null; // no active pregnancy
    }

    /** Update the current week — called as pregnancy progresses. */
    public void updateWeek(int pregnancyId, int week) throws SQLException {

        String sql = "UPDATE pregnancies SET current_week = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, week);
            stmt.setInt(2, pregnancyId);
            stmt.executeUpdate();
        }
    }
}