package com.famille.services;

import com.famille.dao.PushLogDAO;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PushService {

    // Expo's push notification endpoint — no SDK, just a plain HTTP call
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final PushLogDAO pushLogDAO = new PushLogDAO();

    /**
     * Send a push notification via Expo Push API.
     * No Firebase needed — Expo handles routing to Android and iOS.
     *
     * @param expoToken  the device token from Expo (starts with "ExponentPushToken[")
     * @param title      notification title shown in bold
     * @param body       notification message text
     * @param familyId   used for logging
     */
    public void sendPush(String expoToken, String title, String body, int familyId) {

        try {
            // Step 1 — Open HTTP connection to Expo's API
            URL url = new URL(EXPO_PUSH_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept",       "application/json");
            conn.setDoOutput(true); // we are sending a body

            // Step 2 — Build the JSON payload Expo expects
            // Escape any quotes in the message to avoid breaking the JSON
            String safeTitle = title.replace("\"", "\\\"");
            String safeBody  = body.replace("\"",  "\\\"");

            String json = """
                    {
                      "to": "%s",
                      "title": "%s",
                      "body": "%s",
                      "sound": "default",
                      "priority": "high"
                    }
                    """.formatted(expoToken, safeTitle, safeBody);

            // Step 3 — Send the JSON body
            byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBytes);
            }

            // Step 4 — Check the response code
            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                System.out.println("✅ Expo push sent to: " + expoToken);
                pushLogDAO.logPush(familyId, title, body, "sent");
            } else {
                System.err.println("❌ Expo push failed. HTTP " + responseCode);
                pushLogDAO.logPush(familyId, title, body, "failed");
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Expo push error: " + e.getMessage());
            pushLogDAO.logPush(familyId, title, body, "failed");
        }
    }
}