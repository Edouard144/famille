package com.famille.servlets;

import com.famille.dao.OtpDAO;
import com.famille.dao.UserDAO;
import com.famille.models.User;
import com.famille.security.JwtUtil;
import com.famille.services.EmailService;
import com.famille.utils.OtpGenerator;
import com.famille.utils.SessionManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.PrintWriter;

// Handles both /api/auth/signup and /api/auth/login
// Both are whitelisted in AuthFilter — no token needed
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    // DAOs and services this servlet needs
    private final UserDAO      userDAO      = new UserDAO();
    private final OtpDAO       otpDAO       = new OtpDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Read which route was called: /api/auth/signup or /api/auth/login
        String path = request.getPathInfo(); // returns "/signup" or "/login"

        if (path == null) {
            response.setStatus(404);
            out.write("{\"error\": \"Inzira ntiboneka. (Route not found)\"}");
            return;
        }

        switch (path) {
            case "/save-token" -> handleSaveToken(request, response, out);
            case "/signup" -> handleSignup(request, response, out);
            case "/login"  -> handleLogin(request, response, out);
            default -> {
                response.setStatus(404);
                out.write("{\"error\": \"Inzira ntiboneka. (Route not found)\"}");
            }
        }
    }

    // ─────────────────────────────────────────────
    //  SIGNUP
    // ─────────────────────────────────────────────
    private void handleSignup(HttpServletRequest request,
                              HttpServletResponse response,
                              PrintWriter out) throws IOException {
        try {
            // Step 1 — Read the JSON body from the app
            // Expected: { "name": "Mugabo", "email": "...", "password": "...",
            //             "phone": "...", "captchaAnswer": "X7MK2" }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            String name          = body.get("name").getAsString().trim();
            String email         = body.get("email").getAsString().trim().toLowerCase();
            String password      = body.get("password").getAsString();
            String phone         = body.get("phone").getAsString().trim();
            String captchaAnswer = body.get("captchaAnswer").getAsString().trim();

            // Step 2 — Verify CAPTCHA
            // Compares captchaAnswer against what we stored in the session
            if (!SessionManager.verifyCaptcha(request, captchaAnswer)) {
                response.setStatus(400);
                out.write("{\"error\": \"CAPTCHA ntabwo ari yo. Ongera ugerageze. (Wrong CAPTCHA)\"}");
                return;
            }

            // Step 3 — Check if email is already registered
            if (userDAO.findByEmail(email) != null) {
                response.setStatus(409); // 409 = Conflict
                out.write("{\"error\": \"Imeyili isanzwe ikoreshwa. (Email already registered)\"}");
                return;
            }

            // Step 4 — Validate inputs (basic checks)
            if (name.isEmpty() || email.isEmpty() || password.length() < 6) {
                response.setStatus(400);
                out.write("{\"error\": \"Uzuza neza amakuru yose. (Fill all fields correctly)\"}");
                return;
            }

            // Step 5 — Hash the password with BCrypt
            // NEVER store plain passwords — BCrypt scrambles it safely
            // The "10" is the work factor — higher = slower = harder to crack
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(10));

            // Step 6 — Build the User object and save to database
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setPhone(phone);
            user.setVerified(false); // not verified yet — OTP pending

            int userId = userDAO.createUser(user); // returns the new user's ID

            // Step 7 — Generate OTP and save it to otp_codes table
            String otpCode = OtpGenerator.generate();
            otpDAO.saveOtp(userId, otpCode);

            // Step 8 — Send OTP to the parent's email
            EmailService.sendOtpEmail(email, otpCode, name);

            // Step 9 — Return success with userId
            // App will use this userId when calling /api/auth/verify-otp
            response.setStatus(201); // 201 = Created
            out.write("{\"message\": \"Imeyili yoherejwe. Injiza kode. (Email sent. Enter code.)\", " +
                    "\"userId\": " + userId + "}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // Add this method to AuthServlet.java

    private void handleSaveToken(HttpServletRequest request,
                                 HttpServletResponse response,
                                 PrintWriter out) throws IOException {
        try {
            // userId is already attached by AuthFilter — token was validated
            int userId = (int) request.getAttribute("userId");

            // Expected: { "fcmToken": "ExponentPushToken[xxxxx]" }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            String expoToken = body.get("fcmToken").getAsString().trim();

            userDAO.updateFcmToken(userId, expoToken);

            response.setStatus(200);
            out.write("{\"message\": \"Token yabitswe. (Token saved)\"}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────────
    private void handleLogin(HttpServletRequest request,
                             HttpServletResponse response,
                             PrintWriter out) throws IOException {
        try {
            // Step 1 — Read the JSON body
            // Expected: { "email": "mugabo@gmail.com", "password": "abc123" }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            String email    = body.get("email").getAsString().trim().toLowerCase();
            String password = body.get("password").getAsString();

            // Step 2 — Find the user by email
            User user = userDAO.findByEmail(email);

            if (user == null) {
                // Don't say "email not found" — security best practice
                // Always give a vague message so attackers can't probe emails
                response.setStatus(401);
                out.write("{\"error\": \"Imeyili cyangwa ijambo banga si ryo. (Wrong email or password)\"}");
                return;
            }

            // Step 3 — Check the password against the stored hash
            boolean passwordMatches = BCrypt.checkpw(password, user.getPasswordHash());

            if (!passwordMatches) {
                response.setStatus(401);
                out.write("{\"error\": \"Imeyili cyangwa ijambo banga si ryo. (Wrong email or password)\"}");
                return;
            }

            // Step 4 — Check the account is verified
            if (!user.isVerified()) {
                response.setStatus(403); // 403 = Forbidden
                out.write("{\"error\": \"Emeza konti yawe mbere. Reba imeyili yawe. (Verify your account first)\"}");
                return;
            }

            // Step 5 — Everything checks out — generate JWT token
            String token = JwtUtil.generateToken(user.getId(), user.getEmail());

            // Step 6 — Return token + basic user info
            response.setStatus(200);
            out.write("""
                    {
                      "token": "%s",
                      "userId": %d,
                      "name": "%s",
                      "message": "Murakaza neza, %s! (Welcome back, %s!)"
                    }
                    """.formatted(token, user.getId(), user.getName(),
                    user.getName(), user.getName()));

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }
}