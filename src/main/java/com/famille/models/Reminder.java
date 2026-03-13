package com.famille.models;

import java.time.LocalDateTime;

public class Reminder {

    private int id;
    private int familyId;
    private String type;          // "vaccination", "medication", "meal", "antenatal"
    private String message;       // the text sent to the parent
    private LocalDateTime scheduledAt;
    private boolean sent;

    // Extra fields joined from users table — not stored in reminders table
    // Used by ReminderService to know who to call/SMS
    private String parentPhone;
    private String parentName;
    private String fcmToken;      // device token for push notification

    // ── Getters ──────────────────────────────────────────
    public int getId()                    { return id; }
    public int getFamilyId()              { return familyId; }
    public String getType()               { return type; }
    public String getMessage()            { return message; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public boolean isSent()               { return sent; }
    public String getParentPhone()        { return parentPhone; }
    public String getParentName()         { return parentName; }
    public String getFcmToken()           { return fcmToken; }

    // ── Setters ──────────────────────────────────────────
    public void setId(int id)                            { this.id = id; }
    public void setFamilyId(int familyId)                { this.familyId = familyId; }
    public void setType(String type)                     { this.type = type; }
    public void setMessage(String message)               { this.message = message; }
    public void setScheduledAt(LocalDateTime scheduledAt){ this.scheduledAt = scheduledAt; }
    public void setSent(boolean sent)                    { this.sent = sent; }
    public void setParentPhone(String parentPhone)       { this.parentPhone = parentPhone; }
    public void setParentName(String parentName)         { this.parentName = parentName; }
    public void setFcmToken(String fcmToken)             { this.fcmToken = fcmToken; }
}