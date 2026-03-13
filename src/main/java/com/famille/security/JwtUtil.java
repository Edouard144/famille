package com.famille.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // ⚠️ This secret key signs every token — NEVER expose this in production
    // Must be at least 256 bits (32 characters) for HMAC-SHA256
    private static final String SECRET = "famille-secret-key-must-be-long-enough-256bits";

    // Token expires in 7 days (milliseconds)
    private static final long EXPIRY_MS = 7L * 24 * 60 * 60 * 1000;

    // Convert the secret string into a cryptographic signing key
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * Generate a JWT token for a logged-in user.
     * This token is sent to the mobile app and stored there.
     *
     * @param userId  the user's database ID
     * @param email   the user's email
     * @return        a signed JWT string like "eyJhbGci..."
     */
    public static String generateToken(int userId, String email) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))   // who this token belongs to
                .claim("email", email)                 // extra info stored in the token
                .setIssuedAt(new Date())               // when was it created
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRY_MS)) // when it dies
                .signWith(SIGNING_KEY)                 // sign it with our secret key
                .compact();                            // build the final string
    }

    /**
     * Verify a token and extract the claims (data inside it).
     * If the token is fake, expired, or tampered with — this throws an exception.
     *
     * @param token  the JWT string sent by the mobile app
     * @return       the Claims object containing userId, email, expiry, etc.
     * @throws JwtException if anything is wrong with the token
     */
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)  // use the same key to verify the signature
                .build()
                .parseClaimsJws(token)       // this throws if invalid/expired
                .getBody();                  // return the data inside
    }

    /**
     * Quick helper — extract just the user ID from a valid token.
     *
     * @param token  a valid JWT string
     * @return       the user's ID as an integer
     */
    public static int getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return Integer.parseInt(claims.getSubject());
    }
}