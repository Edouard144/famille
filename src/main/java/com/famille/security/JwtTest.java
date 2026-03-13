package com.famille.security;

public class JwtTest {
    public static void main(String[] args) {

        // Simulate: Mugabo just logged in — generate his token
        String token = JwtUtil.generateToken(1, "mugabo@gmail.com");
        System.out.println("Token: " + token);

        // Simulate: Mugabo sends that token on the next request
        int userId = JwtUtil.getUserIdFromToken(token);
        System.out.println("User ID from token: " + userId); // should print 1

        System.out.println("✅ JWT is working correctly.");
    }
}