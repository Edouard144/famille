package com.famille.models;

import java.time.LocalDate;

public class Vaccination {

    private int id;
    private int childId;
    private String vaccineName;
    private LocalDate dueDate;
    private boolean done;
    private LocalDate doneAt;

    // ── Getters ──────────────────────────────────
    public int getId()                { return id; }
    public int getChildId()           { return childId; }
    public String getVaccineName()    { return vaccineName; }
    public LocalDate getDueDate()     { return dueDate; }
    public boolean isDone()           { return done; }
    public LocalDate getDoneAt()      { return doneAt; }

    // ── Setters ──────────────────────────────────
    public void setId(int id)                        { this.id = id; }
    public void setChildId(int childId)              { this.childId = childId; }
    public void setVaccineName(String vaccineName)   { this.vaccineName = vaccineName; }
    public void setDueDate(LocalDate dueDate)        { this.dueDate = dueDate; }
    public void setDone(boolean done)                { this.done = done; }
    public void setDoneAt(LocalDate doneAt)          { this.doneAt = doneAt; }
}