package com.famille.models;

import java.time.LocalDate;

public class Meal {

    private int id;
    private int childId;
    private LocalDate date;

    // Four meal slots per day
    private String breakfast;
    private String lunch;
    private String dinner;
    private String snacks;

    // ── Getters ──────────────────────────────────
    public int getId()              { return id; }
    public int getChildId()         { return childId; }
    public LocalDate getDate()      { return date; }
    public String getBreakfast()    { return breakfast; }
    public String getLunch()        { return lunch; }
    public String getDinner()       { return dinner; }
    public String getSnacks()       { return snacks; }

    // ── Setters ──────────────────────────────────
    public void setId(int id)                    { this.id = id; }
    public void setChildId(int childId)          { this.childId = childId; }
    public void setDate(LocalDate date)          { this.date = date; }
    public void setBreakfast(String breakfast)   { this.breakfast = breakfast; }
    public void setLunch(String lunch)           { this.lunch = lunch; }
    public void setDinner(String dinner)         { this.dinner = dinner; }
    public void setSnacks(String snacks)         { this.snacks = snacks; }
}