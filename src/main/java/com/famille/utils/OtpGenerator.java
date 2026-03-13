package com.famille.utils;

import java.util.Random;

public class OtpGenerator {

    private static final Random random = new Random();

    /**
     * Generate a random 6-digit OTP code.
     * Example output: "847392"
     */
    public static String generate() {
        // nextInt(900000) gives 0–899999, add 100000 to guarantee 6 digits
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}