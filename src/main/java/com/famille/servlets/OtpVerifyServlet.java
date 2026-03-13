package com.famille.servlets;

import com.famille.dao.OtpDAO;
import com.famille.dao.UserDAO;
import com.famille.models.User;
import com.famille.security.JwtUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

// This route is public — whitelisted in AuthFilter
@WebServlet("/api/auth/verify-otp")
public class OtpVerifyServlet extends HttpServlet {

    private final OtpDAO otpDAO = new OtpDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Step 1 — Read JSON body sent by the app
            // Expected: { "userId": 3, "code": "847392" }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            int userId = body.get("userId").getAsInt();
            String code = body.get("code").getAsString().trim();

            // Step 2 — Check the OTP in the database
            int otpId = otpDAO.findValidOtp(userId, code);

            if (otpId == -1) {
                // Code is wrong, expired, or already used
                response.setStatus(400);
                out.write("{\"error\": \"Kode ntabwo ari yo cyangwa yarangiye. (Invalid or expired code)\"}");
                return;
            }

            // Step 3 — Mark OTP as used (prevent reuse)
            otpDAO.markUsed(otpId);

            // Step 4 — Mark user as verified in the users table
            userDAO.markVerified(userId);

            // Step 5 — Get user email to include in the JWT
            User user = userDAO.findById(userId);
            if (user == null) {
                response.setStatus(500);
                out.write("{\"error\": \"User not found\"}");
                return;
            }
            String email = user.getEmail();

            // Step 6 — Generate JWT token — user is now fully logged in
            String token = JwtUtil.generateToken(userId, email);

            // Step 7 — Return the token to the app
            response.setStatus(200);
            out.write("{\"token\": \"" + token + "\", \"message\": \"Konti yawe yemejwe. (Account verified)\"}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }
}