package com.famille.dao;

import com.famille.db.DatabaseConnection;
import com.famille.models.SleepRecord;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SleepDAO {

    /** Save a sleep record — upsert if same child + date already exists. */
    public void saveSleepRecord(SleepRecord record) throws SQLException {

        String sql = """
                INSERT INTO sleep_records (child_id, date, bedtime, wake_time, nap_time)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (child_id, date)
                DO UPDATE SET
                    bedtime   = EXCLUDED.bedtime,
                    wake_time = EXCLUDED.wake_time,
                    nap_time  = EXCLUDED.nap_time
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, record.getChildId());
            stmt.setDate(2, Date.valueOf(record.getDate()));
            stmt.setTime(3, Time.valueOf(record.getBedtime()));
            stmt.setTime(4, Time.valueOf(record.getWakeTime()));
            stmt.setInt(5, record.getNapMinutes());

            stmt.executeUpdate();
        }
    }

    /** Get the last 7 sleep records for a child. */
    public List<SleepRecord> getRecentRecords(int childId) throws SQLException {

        String sql = """
                SELECT * FROM sleep_records
                WHERE child_id = ?
                ORDER BY date DESC
                LIMIT 7
                """;

        List<SleepRecord> records = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, childId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                records.add(mapRow(rs));
            }
        }
        return records;
    }

    /** Helper — map one row to a SleepRecord object. */
    private SleepRecord mapRow(ResultSet rs) throws SQLException {
        SleepRecord r = new SleepRecord();
        r.setId(rs.getInt("id"));
        r.setChildId(rs.getInt("child_id"));
        r.setNapMinutes(rs.getInt("nap_time"));

        Date date = rs.getDate("date");
        if (date != null) r.setDate(date.toLocalDate());

        Time bedtime = rs.getTime("bedtime");
        if (bedtime != null) r.setBedtime(bedtime.toLocalTime());

        Time wakeTime = rs.getTime("wake_time");
        if (wakeTime != null) r.setWakeTime(wakeTime.toLocalTime());

        return r;
    }
}