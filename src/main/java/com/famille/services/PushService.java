package com.famille.services;

import com.famille.dao.PushLogDAO;
import com.google.firebase.messaging.*;

public class PushService {

    private final PushLogDAO pushLogDAO = new PushLogDAO();

    /**
     * Send a push notification to one device via Firebase FCM.
     *
     * @param fcmToken   the device token stored for this user
     * @param title      notification title (shown in bold)
     * @param body       notification message text
     * @param familyId   used for logging
     */
    public void sendPush(String fcmToken, String title, String body, int familyId) {

        try {
            // Build the FCM message
            Message message = Message.builder()
                    .setToken(fcmToken)          // which device to send to
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)     // bold heading
                                    .setBody(body)       // message text
                                    .build()
                    )
                    // Android specific — makes notification show immediately
                    .setAndroidConfig(
                            AndroidConfig.builder()
                                    .setPriority(AndroidConfig.Priority.HIGH)
                                    .build()
                    )
                    .build();

            // Send it — Firebase handles delivery
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Push sent. Message ID: " + response);

            // Log success to database
            pushLogDAO.logPush(familyId, title, body, "sent");

        } catch (FirebaseMessagingException e) {
            System.err.println("❌ Push failed: " + e.getMessage());
            pushLogDAO.logPush(familyId, title, body, "failed");
        }
    }
}