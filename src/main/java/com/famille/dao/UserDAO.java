package com.famille.dao;

import com.famille.db.DatabaseConnection;
import com.famille.models.User;
import java.sql.*;

public class UserDAO {

    // Save a new user to the database during signup
    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, phone, verified) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getPhone());
            stmt.setBoolean(5, false); // not verified yet — OTP pending

            stmt.executeUpdate();
        }
    }

    // Add this method to your existing UserDAO.java

    /**
     * Find a user by their ID.
     * Used after OTP verification to get the email for JWT generation.
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setPhone(rs.getString("phone"));
                user.setVerified(rs.getBoolean("verified"));
                return user;
            }
        }
        return null;
    }

    // Find a user by email (used during login)
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setPhone(rs.getString("phone"));
                user.setVerified(rs.getBoolean("verified"));
                return user;
            }
        }
        return null; // no user found
    }

    // Mark user as verified after OTP confirmed
    public void markVerified(int userId) throws SQLException {
        String sql = "UPDATE users SET verified = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}