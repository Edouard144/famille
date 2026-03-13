package com.famille.services;

import com.famille.dao.ReminderDAO;
import com.famille.models.Reminder;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// @WebListener — Tomcat starts this automatically when server boots
@WebListener
public class ReminderService implements ServletContextListener {

    // Background thread scheduler — runs tasks on a timer
    private ScheduledExecutorService scheduler;

    private final ReminderDAO reminderDAO = new ReminderDAO();
    private final PushService  pushService = new PushService();
    private final SMSService   smsService  = new SMSService();

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        // Create a single background thread
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Run checkAndSendReminders() every 60 minutes
        // First run happens 1 minute after server starts
        scheduler.scheduleAtFixedRate(
                this::checkAndSendReminders,
                1,    // initial delay — 1 minute after startup
                60,   // repeat every 60 minutes
                TimeUnit.MINUTES
        );

        System.out.println("✅ ReminderService started — checking every 60 minutes.");
    }

    /**
     * The main job — runs every hour.
     * Finds all due reminders and sends them via push + SMS.
     */
    private void checkAndSendReminders() {

        System.out.println("🔔 ReminderService: checking due reminders...");

        try {
            // Get all reminders that are due and not yet sent
            List<Reminder> dueReminders = reminderDAO.getDueReminders();

            if (dueReminders.isEmpty()) {
                System.out.println("   No reminders due right now.");
                return;
            }

            System.out.println("   Found " + dueReminders.size() + " reminder(s) to send.");

            for (Reminder reminder : dueReminders) {
                sendReminder(reminder);
            }

        } catch (Exception e) {
            System.err.println("❌ ReminderService error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send one reminder via BOTH push notification AND SMS.
     * Then mark it as sent so it never fires again.
     */
    private void sendReminder(Reminder reminder) {

        String title   = getTitleForType(reminder.getType()); // Kinyarwanda title
        String message = reminder.getMessage();
        int    familyId = reminder.getFamilyId();

        try {
            // ── Send push notification (Firebase FCM) ──────────────
            if (reminder.getFcmToken() != null && !reminder.getFcmToken().isBlank()) {
                pushService.sendPush(reminder.getFcmToken(), title, message, familyId);
            }

            // ── Send SMS (Twilio) ──────────────────────────────────
            if (reminder.getParentPhone() != null && !reminder.getParentPhone().isBlank()) {

                // SMS combines title + message in one text
                String smsText = title + ": " + message;
                smsService.sendSMS(reminder.getParentPhone(), smsText, familyId);
            }

            // ── Mark as sent — won't fire again ───────────────────
            reminderDAO.markSent(reminder.getId());

            System.out.println("   ✅ Sent reminder ID " + reminder.getId()
                    + " to family " + familyId);

        } catch (Exception e) {
            System.err.println("   ❌ Failed to send reminder ID "
                    + reminder.getId() + ": " + e.getMessage());
            // Don't mark as sent — it will retry next hour
        }
    }

    /**
     * Return the Kinyarwanda notification title based on reminder type.
     */
    private String getTitleForType(String type) {
        return switch (type) {
            case "vaccination"  -> "💉 Igihe cy'inkingo (Vaccination due)";
            case "medication"   -> "💊 Igihe cy'umuti (Medication time)";
            case "meal"         -> "🍽️ Igihe cy'ifunguro (Meal time)";
            case "antenatal"    -> "🤰 Isura ry'inda (Antenatal visit)";
            case "sleep"        -> "😴 Igihe cyo kuryama (Bedtime)";
            case "deworming"    -> "💊 Igihe cyo gukuraho inzoka (Deworming)";
            case "vitamin_a"    -> "🌟 Vitamin A — igihe cyayo";
            default             -> "🔔 Ibibutso (Reminder) — Famille";
        };
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Shut down the background thread cleanly when server stops
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("🛑 ReminderService stopped.");
        }
    }
}