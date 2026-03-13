package com.famille.dao;

import com.famille.db.DatabaseConnection;
import com.famille.models.Child;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChildDAO {

    /**
     * Save a new child to the database.
     * Returns the new child's ID.
     */
    public int createChild(Child child) throws SQLException {

        String sql = "INSERT INTO children (family_id, name, birth_date, blood_type, gender) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, child.getFamilyId());
            stmt.setString(2, child.getName());
            stmt.setDate(3, Date.valueOf(child.getBirthDate()));
            stmt.setString(4, child.getBloodType());
            stmt.setString(5, child.getGender());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        throw new SQLException("Umwana ntiyashyizweho. (Child creation failed)");
    }

    /**
     * Get all children belonging to a family.
     * Called when the parent opens the children screen.
     */
    public List<Child> getChildrenByFamily(int familyId) throws SQLException {

        String sql = "SELECT * FROM children WHERE family_id = ? ORDER BY birth_date ASC";
        List<Child> children = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, familyId);
            ResultSet rs = stmt.executeQuery();

            // Loop through every row and build a Child object
            while (rs.next()) {
                children.add(mapRow(rs));
            }
        }
        return children;
    }

    /**
     * Get one child by their ID.
     * Used when opening a specific child's profile.
     */
    public Child findById(int childId) throws SQLException {

        String sql = "SELECT * FROM children WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, childId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    /**
     * Update a child's information.
     * Called when the parent edits the child's profile.
     */
    public void updateChild(Child child) throws SQLException {

        String sql = "UPDATE children SET name=?, birth_date=?, blood_type=?, gender=? " +
                "WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, child.getName());
            stmt.setDate(2, Date.valueOf(child.getBirthDate()));
            stmt.setString(3, child.getBloodType());
            stmt.setString(4, child.getGender());
            stmt.setInt(5, child.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * Helper — convert one database row into a Child object.
     * Used in every method above to avoid repeating the same mapping code.
     */
    private Child mapRow(ResultSet rs) throws SQLException {
        Child child = new Child();
        child.setId(rs.getInt("id"));
        child.setFamilyId(rs.getInt("family_id"));
        child.setName(rs.getString("name"));
        child.setGender(rs.getString("gender"));
        child.setBloodType(rs.getString("blood_type"));

        // Convert SQL Date → Java LocalDate
        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            child.setBirthDate(birthDate.toLocalDate());
        }
        return child;
    }
}