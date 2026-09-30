package com.kristalball.militaryasset.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for creating and validating JWT tokens.
 *
 * - Tokens are signed with HMAC-SHA256 using the secret from application.properties.
 * - The token subject is the user's email address.
 * - The token also stores the user's role as a claim so the frontend can use it.
 */
@Component
public class JwtUtil {

    // Injected from application.properties: jwt.secret
    @Value("${jwt.secret}")
    private String secret;

    // Injected from application.properties: jwt.expiration (in milliseconds)
    @Value("${jwt.expiration}")
    private long expirationMs;

    /** Build a signing key from the configured secret string. */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT token for the given email and role.
     *
     * @param email the user's email (used as the token subject)
     * @param role  the user's role (stored as a custom claim)
     * @return a signed JWT string
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract the email (subject) from a token.
     * Throws JwtException if the token is invalid or expired.
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extract the role claim from a token.
     */
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Validate a token: checks signature and expiration.
     *
     * @return true if the token is valid, false if it is expired or tampered with
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token); // throws if invalid
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Parse and return all claims from the token. Throws if invalid. */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
