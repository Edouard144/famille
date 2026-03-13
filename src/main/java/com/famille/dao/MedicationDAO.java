package com.famille.dao;

import com.famille.db.DatabaseConnection;
import com.famille.models.Medication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicationDAO {

    /** Save a new medication reminder for a child. */
    public int saveMedication(Medication med) throws SQLException {

        String sql = """
                INSERT INTO medications
                    (child_id, name, dose, frequency, start_date, end_date)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, med.getChildId());
            stmt.setString(2, med.getName());
            stmt.setString(3, med.getDose());
            stmt.setString(4, med.getFrequency());
            stmt.setDate(5, Date.valueOf(med.getStartDate()));

            // End date can be null for ongoing medications
            if (med.getEndDate() != null) {
                stmt.setDate(6, Date.valueOf(med.getEndDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        throw new SQLException("Umuti ntiwashyizweho. (Medication save failed)");
    }

    /**
     * Get all ACTIVE medications for a child.
     * Active = today is between start_date and end_date.
     */
    public List<Medication> getActiveMedications(int childId) throws SQLException {

        // Only return medications that haven't ended yet
        String sql = """
                SELECT * FROM medications
                WHERE child_id = ?
                  AND (end_date IS NULL OR end_date >= CURRENT_DATE)
                ORDER BY start_date DESC
                """;

        List<Medication> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, childId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Remove a medication — called when a course is completed. */
    public void deleteMedication(int medicationId) throws SQLException {

        String sql = "DELETE FROM medications WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicationId);
            stmt.executeUpdate();
        }
    }

    /** Helper — map one row to a Medication object. */
    private Medication mapRow(ResultSet rs) throws SQLException {
        Medication med = new Medication();
        med.setId(rs.getInt("id"));
        med.setChildId(rs.getInt("child_id"));
        med.setName(rs.getString("name"));
        med.setDose(rs.getString("dose"));
        med.setFrequency(rs.getString("frequency"));

        Date start = rs.getDate("start_date");
        if (start != null) med.setStartDate(start.toLocalDate());

        Date end = rs.getDate("end_date");
        if (end != null) med.setEndDate(end.toLocalDate());

        return med;
    }
}