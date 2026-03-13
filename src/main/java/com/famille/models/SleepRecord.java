package com.famille.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class SleepRecord {

    private int id;
    private int childId;
    private LocalDate date;
    private LocalTime bedtime;
    private LocalTime wakeTime;
    private int napMinutes; // nap duration in minutes

    // ── Getters ──────────────────────────────────
    public int getId()              { return id; }
    public int getChildId()         { return childId; }
    public LocalDate getDate()      { return date; }
    public LocalTime getBedtime()   { return bedtime; }
    public LocalTime getWakeTime()  { return wakeTime; }
    public int getNapMinutes()      { return napMinutes; }

    // ── Setters ──────────────────────────────────
    public void setId(int id)                    { this.id = id; }
    public void setChildId(int childId)          { this.childId = childId; }
    public void setDate(LocalDate date)          { this.date = date; }
    public void setBedtime(LocalTime bedtime)    { this.bedtime = bedtime; }
    public void setWakeTime(LocalTime wakeTime)  { this.wakeTime = wakeTime; }
    public void setNapMinutes(int napMinutes)    { this.napMinutes = napMinutes; }

    /**
     * Calculate total sleep hours for the night.
     * Night sleep + nap time combined.
     */
    public double getTotalSleepHours() {
        if (bedtime == null || wakeTime == null) return 0;

        // Handle overnight sleep (bedtime after midnight counts forward)
        // e.g. bedtime 20:00, wake 06:00 = 10 hours
        int bedMinutes  = bedtime.getHour()  * 60 + bedtime.getMinute();
        int wakeMinutes = wakeTime.getHour() * 60 + wakeTime.getMinute();

        // If wake time is before bedtime, it crossed midnight
        if (wakeMinutes < bedMinutes) {
            wakeMinutes += 24 * 60; // add 24 hours
        }

        int nightMinutes = wakeMinutes - bedMinutes;
        int totalMinutes = nightMinutes + napMinutes;

        // Return as hours rounded to 1 decimal
        return Math.round((totalMinutes / 60.0) * 10.0) / 10.0;
    }

    /**
     * Get the recommended sleep hours for this child's age.
     * Based on WHO / AAP guidelines.
     *
     * @param ageInMonths  child's age
     * @return             recommended hours as a string like "12–16"
     */
    public static String getRecommendedHours(int ageInMonths) {
        if (ageInMonths < 4)   return "16–18"; // newborn
        if (ageInMonths < 12)  return "12–16"; // infant
        if (ageInMonths < 36)  return "11–14"; // toddler
        if (ageInMonths < 72)  return "10–13"; // preschool
        if (ageInMonths < 156) return "9–12";  // school age
        return "8–10";                          // teen
    }
}