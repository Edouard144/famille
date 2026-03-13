package com.famille.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Pregnancy {

    private int id;
    private int familyId;
    private LocalDate dueDate;
    private int currentWeek;
    private LocalDate createdAt;

    // ── Getters ──────────────────────────────────
    public int getId()              { return id; }
    public int getFamilyId()        { return familyId; }
    public LocalDate getDueDate()   { return dueDate; }
    public int getCurrentWeek()     { return currentWeek; }
    public LocalDate getCreatedAt() { return createdAt; }

    // ── Setters ──────────────────────────────────
    public void setId(int id)                      { this.id = id; }
    public void setFamilyId(int familyId)          { this.familyId = familyId; }
    public void setDueDate(LocalDate dueDate)      { this.dueDate = dueDate; }
    public void setCurrentWeek(int currentWeek)    { this.currentWeek = currentWeek; }
    public void setCreatedAt(LocalDate createdAt)  { this.createdAt = createdAt; }

    /**
     * Calculate how many weeks pregnant automatically from the due date.
     * A full pregnancy is 40 weeks — count backwards from due date.
     */
    public int calculateCurrentWeek() {
        if (dueDate == null) return 0;
        // Conception is roughly 40 weeks before due date
        LocalDate conceptionDate = dueDate.minusWeeks(40);
        long weeksPassed = ChronoUnit.WEEKS.between(conceptionDate, LocalDate.now());
        // Clamp between 1 and 42 weeks
        return (int) Math.min(Math.max(weeksPassed, 1), 42);
    }
}