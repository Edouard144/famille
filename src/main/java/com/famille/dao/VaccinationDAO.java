package com.famille.dao;

import com.famille.db.DatabaseConnection;
import com.famille.models.Vaccination;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VaccinationDAO {

    /**
     * Save a single vaccine entry.
     * Called in a loop when a child is first created.
     */
    public void saveVaccination(Vaccination v) throws SQLException {

        String sql = "INSERT INTO vaccinations (child_id, vaccine_name, due_date, done) " +
                "VALUES (?, ?, ?, FALSE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, v.getChildId());
            stmt.setString(2, v.getVaccineName());
            stmt.setDate(3, Date.valueOf(v.getDueDate()));
            stmt.executeUpdate();
        }
    }

    /** Get the full vaccination schedule for one child. */
    public List<Vaccination> getByChildId(int childId) throws SQLException {

        String sql = "SELECT * FROM vaccinations WHERE child_id = ? ORDER BY due_date ASC";
        List<Vaccination> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, childId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vaccination v = new Vaccination();
                v.setId(rs.getInt("id"));
                v.setChildId(rs.getInt("child_id"));
                v.setVaccineName(rs.getString("vaccine_name"));
                v.setDone(rs.getBoolean("done"));

                Date due = rs.getDate("due_date");
                if (due != null) v.setDueDate(due.toLocalDate());

                Date doneAt = rs.getDate("done_at");
                if (doneAt != null) v.setDoneAt(doneAt.toLocalDate());

                list.add(v);
            }
        }
        return list;
    }

    /** Mark a vaccine as done with today's date. */
    public void markDone(int vaccinationId) throws SQLException {

        String sql = "UPDATE vaccinations SET done = TRUE, done_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, vaccinationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Generate and save the full Rwanda MOH vaccination schedule for a child.
     * Called automatically when a new child is created.
     *
     * Rwanda MOH schedule based on child's birth date.
     */
    public void generateSchedule(int childId, LocalDate birthDate) throws SQLException {

        // Rwanda Ministry of Health standard vaccination schedule
        // Format: vaccine name → weeks after birth
        Object[][] schedule = {
                // At birth
                {"BCG (Tuberculosis)",        0},
                {"Polio 0 (OPV)",             0},
                {"Hepatitis B (Birth dose)",  0},

                // 6 weeks
                {"Pentavalent 1 (DTP-HepB-Hib)", 6},
                {"Polio 1 (OPV)",                 6},
                {"Rotavirus 1",                   6},
                {"PCV 1 (Pneumococcal)",          6},

                // 10 weeks
                {"Pentavalent 2",   10},
                {"Polio 2 (OPV)",   10},
                {"Rotavirus 2",     10},
                {"PCV 2",           10},

                // 14 weeks
                {"Pentavalent 3",    14},
                {"Polio 3 (OPV)",    14},
                {"IPV (Inactivated Polio)", 14},
                {"PCV 3",            14},

                // 9 months
                {"Measles / Rubella 1",  39},
                {"Yellow Fever",         39},
                {"Vitamin A (1st dose)", 39},

                // 15 months
                {"Measles / Rubella 2",  65},
                {"Vitamin A (2nd dose)", 65},

                // 24 months — deworming
                {"Deworming (Mebendazole)", 104},
        };

        for (Object[] entry : schedule) {
            String vaccineName = (String) entry[0];
            int    weeksAfter  = (int)    entry[1];

            Vaccination v = new Vaccination();
            v.setChildId(childId);
            v.setVaccineName(vaccineName);
            v.setDueDate(birthDate.plusWeeks(weeksAfter));

            saveVaccination(v);
        }
    }
}