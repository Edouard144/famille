package com.famille.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

// @WebFilter tells Tomcat: intercept ALL requests to /api/*
// EXCEPT the ones we whitelist below
@WebFilter("/api/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Cast to HTTP versions so we can read headers
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // Always return JSON, even for errors
        httpResp.setContentType("application/json");
        httpResp.setCharacterEncoding("UTF-8");

        String path = httpReq.getServletPath();

        // ✅ WHITELIST — these routes don't need a token (public endpoints)
        if (isPublicRoute(path)) {
            chain.doFilter(request, response); // let it through without checking
            return;
        }

        // 🔍 Read the Authorization header
        // Mobile app sends: Authorization: Bearer eyJhbGci...
        String authHeader = httpReq.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token at all — block the request
            sendError(httpResp, 401, "Ntabwo winjiye. Injira mbere. (No token provided)");
            return;
        }

        // Strip "Bearer " prefix to get the raw token
        String token = authHeader.substring(7);

        try {
            // Validate the token — throws exception if bad/expired/fake
            Claims claims = JwtUtil.validateToken(token);

            // ✅ Token is valid — attach user info to the request
            // Any servlet can now read: request.getAttribute("userId")
            httpReq.setAttribute("userId", Integer.parseInt(claims.getSubject()));
            httpReq.setAttribute("email", claims.get("email", String.class));

            // Pass the request forward to the actual servlet
            chain.doFilter(request, response);

        } catch (JwtException e) {
            // Token exists but is invalid, expired, or tampered with
            sendError(httpResp, 401, "Uruhushya rwawe rwarangiye cyangwa si rwe. (Invalid or expired token)");
        } catch (Exception e) {
            // Handle any unexpected errors
            sendError(httpResp, 500, "Internal server error");
        }
    }

    /**
     * Routes that don't require authentication.
     * These are the signup, login, OTP, and CAPTCHA endpoints.
     */
    private boolean isPublicRoute(String path) {
        return path.equals("/api/auth/signup")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/verify-otp")
                || path.equals("/api/captcha");
    }

    /**
     * Send a clean JSON error response.
     * This is what the mobile app receives when a request is blocked.
     */
    private void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        // Simple JSON error — no library needed for this
        out.write("{\"error\": \"" + message + "\"}");
        out.flush();
    }
}