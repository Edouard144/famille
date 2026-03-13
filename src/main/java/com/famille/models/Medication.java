package com.famille.models;

import java.time.LocalDate;

public class Medication {

    private int id;
    private int childId;
    private String name;        // e.g. "Amoxicillin"
    private String dose;        // e.g. "5ml"
    private String frequency;   // e.g. "3x per day"
    private LocalDate startDate;
    private LocalDate endDate;

    // ── Getters ──────────────────────────────────
    public int getId()               { return id; }
    public int getChildId()          { return childId; }
    public String getName()          { return name; }
    public String getDose()          { return dose; }
    public String getFrequency()     { return frequency; }
    public LocalDate getStartDate()  { return startDate; }
    public LocalDate getEndDate()    { return endDate; }

    // ── Setters ──────────────────────────────────
    public void setId(int id)                      { this.id = id; }
    public void setChildId(int childId)            { this.childId = childId; }
    public void setName(String name)               { this.name = name; }
    public void setDose(String dose)               { this.dose = dose; }
    public void setFrequency(String frequency)     { this.frequency = frequency; }
    public void setStartDate(LocalDate startDate)  { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate)      { this.endDate = endDate; }

    /**
     * Check if this medication is still active today.
     * Used to filter out expired courses.
     */
    public boolean isActive() {
        if (endDate == null) return true; // no end date = ongoing
        return !LocalDate.now().isAfter(endDate);
    }
}