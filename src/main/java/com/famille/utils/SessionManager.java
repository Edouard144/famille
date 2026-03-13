package com.famille.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionManager {

    /**
     * Check if the CAPTCHA answer the user typed matches what we stored.
     *
     * @param request      the HTTP request (to access the session)
     * @param userAnswer   what the parent typed in the app
     * @return             true if correct, false if wrong or expired
     */
    public static boolean verifyCaptcha(HttpServletRequest request, String userAnswer) {

        HttpSession session = request.getSession(false); // don't create new session

        if (session == null) return false; // session expired — must reload CAPTCHA

        String storedAnswer = (String) session.getAttribute("captchaAnswer");

        if (storedAnswer == null) return false; // no CAPTCHA was generated

        // Remove it after checking — prevents reuse
        session.removeAttribute("captchaAnswer");

        // Compare — ignore case so "x7mk2" matches "X7MK2"
        return storedAnswer.equalsIgnoreCase(userAnswer.trim());
    }
}