package com.famille.dao;

import com.famille.db.DatabaseConnection;
import com.famille.models.Reminder;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReminderDAO {

    /**
     * Save a new reminder to be sent in the future.
     * Called whenever a vaccination, medication, or meal event is created.
     */
    public void saveReminder(Reminder reminder) throws SQLException {

        String sql = """
                INSERT INTO reminders (family_id, type, message, scheduled_at, sent)
                VALUES (?, ?, ?, ?, FALSE)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reminder.getFamilyId());
            stmt.setString(2, reminder.getType());
            stmt.setString(3, reminder.getMessage());
            stmt.setTimestamp(4, Timestamp.valueOf(reminder.getScheduledAt()));

            stmt.executeUpdate();
        }
    }

    /**
     * Get all reminders that are due NOW and haven't been sent yet.
     * This is what ReminderService checks every hour.
     */
    public List<Reminder> getDueReminders() throws SQLException {

        // Find reminders where the scheduled time has passed and not yet sent
        String sql = """
                SELECT r.*, u.phone, u.name as parent_name,
                       f.id as fam_id
                FROM reminders r
                JOIN families f ON r.family_id = f.id
                JOIN users    u ON f.user_id   = u.id
                WHERE r.scheduled_at <= NOW()
                  AND r.sent = FALSE
                ORDER BY r.scheduled_at ASC
                """;

        List<Reminder> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reminder r = new Reminder();
                r.setId(rs.getInt("id"));
                r.setFamilyId(rs.getInt("family_id"));
                r.setType(rs.getString("type"));
                r.setMessage(rs.getString("message"));
                r.setParentPhone(rs.getString("phone"));
                r.setParentName(rs.getString("parent_name"));

                Timestamp scheduledAt = rs.getTimestamp("scheduled_at");
                if (scheduledAt != null) {
                    r.setScheduledAt(scheduledAt.toLocalDateTime());
                }

                list.add(r);
            }
        }
        return list;
    }

    /** Mark a reminder as sent so it never fires again. */
    public void markSent(int reminderId) throws SQLException {

        String sql = "UPDATE reminders SET sent = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reminderId);
            stmt.executeUpdate();
        }
    }
}