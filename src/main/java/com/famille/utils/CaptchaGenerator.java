package com.famille.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaGenerator {

    // Image size
    private static final int WIDTH  = 200;
    private static final int HEIGHT = 60;

    // Characters to pick from — removed confusing ones like 0/O, 1/l/I
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final Random random = new Random();

    /**
     * Generate a random 5-character CAPTCHA text.
     * This is the "answer" — stored in session, compared when user submits.
     */
    public static String generateText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString(); // e.g. "X7MK2"
    }

    /**
     * Draw the CAPTCHA text onto an image with noise and distortion.
     * Returns a BufferedImage that we convert to PNG and send to the app.
     *
     * @param text  the CAPTCHA answer text (from generateText())
     * @return      a distorted image of that text
     */
    public static BufferedImage generateImage(String text) {

        // Create a blank white image
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // --- Background ---
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // --- Draw random noise lines to make it harder for bots ---
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 8; i++) {
            // Random color for each line
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(
                    random.nextInt(WIDTH), random.nextInt(HEIGHT), // start point
                    random.nextInt(WIDTH), random.nextInt(HEIGHT)  // end point
            );
        }

        // --- Draw random noise dots ---
        for (int i = 0; i < 80; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }

        // --- Draw each character with slight rotation and position variation ---
        g.setFont(new Font("Arial", Font.BOLD, 32));

        for (int i = 0; i < text.length(); i++) {

            // Each character gets a slightly different color (dark so it's readable)
            g.setColor(new Color(
                    random.nextInt(100),       // R — keep dark
                    random.nextInt(100),       // G — keep dark
                    random.nextInt(100)        // B — keep dark
            ));

            // Rotate each character slightly — between -25 and +25 degrees
            double angle = Math.toRadians(random.nextInt(50) - 25);

            // Calculate where to draw this character
            int x = 20 + (i * 35);        // spread characters across the image
            int y = 40 + random.nextInt(10) - 5; // slight vertical variation

            // Apply rotation around the character's position
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(text.charAt(i)), x, y);

            // Reset rotation so the next character starts fresh
            g.rotate(-angle, x, y);
        }

        g.dispose(); // free memory
        return image;
    }
}