package com.famille.models;

import java.time.LocalDate;

public class Child {

    private int id;
    private int familyId;
    private String name;
    private LocalDate birthDate;  // used to calculate age automatically
    private String bloodType;
    private String gender;

    // ── Getters ──────────────────────────────────
    public int getId()                { return id; }
    public int getFamilyId()          { return familyId; }
    public String getName()           { return name; }
    public LocalDate getBirthDate()   { return birthDate; }
    public String getBloodType()      { return bloodType; }
    public String getGender()         { return gender; }

    // ── Setters ──────────────────────────────────
    public void setId(int id)                      { this.id = id; }
    public void setFamilyId(int familyId)          { this.familyId = familyId; }
    public void setName(String name)               { this.name = name; }
    public void setBirthDate(LocalDate birthDate)  { this.birthDate = birthDate; }
    public void setBloodType(String bloodType)     { this.bloodType = bloodType; }
    public void setGender(String gender)           { this.gender = gender; }

    /**
     * Calculate the child's age in months from their birth date.
     * Used to pick the right vaccine schedule and meal suggestions.
     */
    public int getAgeInMonths() {
        if (birthDate == null) return 0;
        LocalDate today = LocalDate.now();
        int years  = today.getYear()       - birthDate.getYear();
        int months = today.getMonthValue() - birthDate.getMonthValue();
        return (years * 12) + months;
    }
}