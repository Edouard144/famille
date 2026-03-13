package com.famille.dao;

import com.famille.db.DatabaseConnection;
import com.famille.models.Meal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MealDAO {

    /**
     * Save today's meals for a child.
     * If a record already exists for this date, update it instead.
     * This is called an UPSERT — insert OR update.
     */
    public void saveMeal(Meal meal) throws SQLException {

        // PostgreSQL UPSERT — if same child + date exists, update it
        String sql = """
                INSERT INTO meals (child_id, date, breakfast, lunch, dinner, snacks)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (child_id, date)
                DO UPDATE SET
                    breakfast = EXCLUDED.breakfast,
                    lunch     = EXCLUDED.lunch,
                    dinner    = EXCLUDED.dinner,
                    snacks    = EXCLUDED.snacks
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, meal.getChildId());
            stmt.setDate(2, Date.valueOf(meal.getDate()));
            stmt.setString(3, meal.getBreakfast());
            stmt.setString(4, meal.getLunch());
            stmt.setString(5, meal.getDinner());
            stmt.setString(6, meal.getSnacks());

            stmt.executeUpdate();
        }
    }

    /**
     * Get the last 7 days of meals for a child.
     * Used to show the weekly meal history screen.
     */
    public List<Meal> getRecentMeals(int childId) throws SQLException {

        String sql = """
                SELECT * FROM meals
                WHERE child_id = ?
                ORDER BY date DESC
                LIMIT 7
                """;

        List<Meal> meals = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, childId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                meals.add(mapRow(rs));
            }
        }
        return meals;
    }

    /**
     * Get meals for a specific date.
     * Used to check what a child ate on a particular day.
     */
    public Meal getMealByDate(int childId, LocalDate date) throws SQLException {

        String sql = "SELECT * FROM meals WHERE child_id = ? AND date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, childId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    /** Helper — map one database row to a Meal object. */
    private Meal mapRow(ResultSet rs) throws SQLException {
        Meal meal = new Meal();
        meal.setId(rs.getInt("id"));
        meal.setChildId(rs.getInt("child_id"));
        meal.setBreakfast(rs.getString("breakfast"));
        meal.setLunch(rs.getString("lunch"));
        meal.setDinner(rs.getString("dinner"));
        meal.setSnacks(rs.getString("snacks"));

        Date date = rs.getDate("date");
        if (date != null) meal.setDate(date.toLocalDate());

        return meal;
    }
}