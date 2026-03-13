package com.famille.services;

import com.famille.dao.SMSLogDAO;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSService {

    // Get all three from your Twilio dashboard
    private static final String ACCOUNT_SID  = "your_twilio_account_sid";
    private static final String AUTH_TOKEN   = "your_twilio_auth_token";
    private static final String FROM_NUMBER  = "+1234567890"; // your Twilio number

    // Initialize Twilio once when class loads
    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    private final SMSLogDAO smsLogDAO = new SMSLogDAO();

    /**
     * Send an SMS reminder to a parent's phone number.
     *
     * @param toPhone   parent's phone number (e.g. "+250788123456")
     * @param message   the SMS text (written in Kinyarwanda)
     * @param familyId  used for logging
     */
    public void sendSMS(String toPhone, String message, int familyId) {

        try {
            // Send SMS via Twilio API
            Message.creator(
                    new PhoneNumber(toPhone),    // recipient
                    new PhoneNumber(FROM_NUMBER), // sender (your Twilio number)
                    message                       // SMS body
            ).create();

            System.out.println("✅ SMS sent to " + toPhone);

            // Log the success
            smsLogDAO.logSMS(familyId, message, toPhone, "sent");

        } catch (Exception e) {
            System.err.println("❌ SMS failed to " + toPhone + ": " + e.getMessage());
            smsLogDAO.logSMS(familyId, message, toPhone, "failed");
        }
    }
}