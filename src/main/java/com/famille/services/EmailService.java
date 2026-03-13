package com.famille.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailService {

    // Your Gmail address that sends the OTP emails
    private static final String FROM_EMAIL = "edouardtuyubahe@gmail.com";

    // App Password from Google (NOT your real Gmail password)
    // Google Account → Security → 2-Step Verification → App Passwords
    private static final String APP_PASSWORD = "qfly nezm dles mwty";

    /**
     * Send an OTP verification email to a new parent.
     * Written in Kinyarwanda with the code clearly displayed.
     *
     * @param toEmail the parent's email address
     * @param code    the 6-digit OTP code
     * @param name    the parent's name (for personalization)
     */
    public static void sendOtpEmail(String toEmail, String code, String name)
            throws MessagingException, UnsupportedEncodingException {

        // Step 1 — Configure Gmail SMTP connection properties
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // Gmail SMTP server
        props.put("mail.smtp.port", "587");            // TLS port
        props.put("mail.smtp.auth", "true");           // require login
        props.put("mail.smtp.starttls.enable", "true");// encrypt connection

        // Step 2 — Create an authenticated mail session
        Session mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        // Step 3 — Build the email message
        Message message = new MimeMessage(mailSession);

        // Set sender with display name
        message.setFrom(new InternetAddress(FROM_EMAIL, "Famille App"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Kode y'Inyemezabuguzi / Verification Code");

        // Email body — Kinyarwanda first, English below
        String body = """
                Muraho %s,

                Kode yawe yo kwemeza konti ni:

                ┌─────────────────┐
                │     %s          │
                └─────────────────┘

                Iyi kode izarangira mu minota 10.
                Ntuyisangire na muntu wese.

                ─────────────────────────
                Hello %s,
                Your verification code is: %s
                This code expires in 10 minutes.
                Do not share it with anyone.

                — Famille App 🏠
                """.formatted(name, code, name, code);

        message.setContent(body, "text/plain; charset=UTF-8");

        // Step 4 — Send it
        Transport.send(message);
    }
}
